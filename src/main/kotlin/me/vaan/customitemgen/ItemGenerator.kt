package me.vaan.customitemgen

import io.github.seggan.sf4k.extensions.getSlimefun
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.ItemState
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu.AdvancedMenuClickHandler
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.interfaces.InventoryBlock
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker
import me.mrCookieSlime.Slimefun.api.BlockStorage
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow
import org.apache.commons.lang3.Validate
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class ItemGenerator(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType?,
    recipe: Array<ItemStack?>?,
    progressBar: ItemStack,
    val production: List<GenEntry>
) : SlimefunItem(itemGroup, item, recipeType, recipe),
    InventoryBlock, EnergyNetComponent, MachineProcessHolder<CraftingOperation>, RecipeDisplayItem {

    companion object {
        private const val PROGRESS_SLOT = 12
        private val BORDER = intArrayOf(3, 21)
        private val BORDER_IN = intArrayOf(0, 1, 2, 9, 11, 18, 19, 20)
        private val BORDER_OUT = intArrayOf(4, 5, 6, 7, 8, 22, 23, 24, 25, 26)
    }

    private val KEY_POSITION = "current-position"
    private val KEY_CONSUMPTION = "current-consumption"

    private val processor = MachineProcessor(this)
    private var currentConsumption = 0
    private var currentPosition = 0

    private val energyConsumption: Int
        get() {
            return currentConsumption
        }

    var energyCapacity = -1
        private set

    var speed: Int = 1
        private set

    init {
        processor.progressBar = progressBar

        object : BlockMenuPreset(this.id, inventoryTitle) {
            override fun init() {
                constructMenu(this)
            }

            override fun getSlotsAccessedByItemTransport(flow: ItemTransportFlow): IntArray {
                return if (flow == ItemTransportFlow.INSERT) {
                    inputSlots
                } else {
                    outputSlots
                }
            }

            override fun newInstance(menu: BlockMenu, block: Block) {
                val baseItem = production[0].recipe.input[0]

                menu.addItem(inputSlots[0], baseItem) { _: Player?, slot: Int, clickedStack: ItemStack?, _: ClickAction? ->
                    this@ItemGenerator.incrementPosition()
                    BlockStorage.addBlockInfo(block, KEY_POSITION, currentPosition.toString())
                    BlockStorage.addBlockInfo(block, KEY_CONSUMPTION, currentConsumption.toString())

                    menu.replaceExistingItem(slot, production[currentPosition].recipe.input[0])
                    false
                }
            }

            override fun canOpen(b: Block, p: Player): Boolean {
                if (p.hasPermission("slimefun.inventory.bypass")) {
                    return true
                }

                return this@ItemGenerator.canUse(p, false) && (// Protection manager doesn't exist in unit tests
                        Slimefun.instance()!!.isUnitTest
                            || Slimefun.getProtectionManager()
                            .hasPermission(p, b.location, Interaction.INTERACT_BLOCK))
            }
        }

        addItemHandler(onBlockBreak(), onBlockPlace())
    }

    private fun onBlockBreak(): BlockBreakHandler {
        return object : SimpleBlockBreakHandler() {
            override fun onBlockBreak(b: Block) {
                val inv = BlockStorage.getInventory(b)

                inv?.dropItems(b.location, *outputSlots)

                processor.endOperation(b)
            }
        }
    }

    private fun onBlockPlace(): BlockPlaceHandler {
        return object : BlockPlaceHandler(true) {
            override fun onPlayerPlace(p0: BlockPlaceEvent) {
                this@ItemGenerator.tick(p0.block)
            }
        }
    }

    private fun incrementPosition() {
        currentPosition = (currentPosition + 1) % production.size
    }

    override fun getMachineProcessor(): MachineProcessor<CraftingOperation> {
        return processor
    }

    private fun constructMenu(preset: BlockMenuPreset) {
        for (i in BORDER) {
            preset.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler())
        }

        for (i in BORDER_IN) {
            preset.addItem(i, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler())
        }

        for (i in BORDER_OUT) {
            preset.addItem(i, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler())
        }

        preset.addItem(
            PROGRESS_SLOT,
            CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " "),
            ChestMenuUtils.getEmptyClickHandler()
        )

        preset.addItem(inputSlots[0], production[0].recipe.input[0])

        val outputHandler = object : AdvancedMenuClickHandler {
            override fun onClick(p: Player, slot: Int, cursor: ItemStack, action: ClickAction): Boolean {
                return false
            }

            override fun onClick(
                e: InventoryClickEvent,
                p: Player,
                slot: Int,
                cursor: ItemStack?,
                action: ClickAction
            ): Boolean {
                return cursor?.type == null || cursor.type == Material.AIR
            }
        }
        for (i in outputSlots) {
            preset.addMenuClickHandler(i, outputHandler)
        }
    }

    val inventoryTitle: String
        get() = itemName


    fun setCapacity(capacity: Int): ItemGenerator {
        Validate.isTrue(capacity > 0, "The capacity must be greater than zero!")

        check(state == ItemState.UNREGISTERED) { "You cannot modify the capacity after the Item was registered." }

        this.energyCapacity = capacity
        return this
    }

    fun setProcessingSpeed(speed: Int): ItemGenerator {
        Validate.isTrue(speed > 0, "The speed must be greater than zero!")

        this.speed = speed
        return this
    }

    override fun register(addon: SlimefunAddon) {
        this.addon = addon
        super.register(addon)

        if (capacity <= 0) {
            warn("The capacity has not been configured correctly. The Item was disabled.")
            warn("Make sure to call '" + this.item.getSlimefun<SlimefunItem>()!!.id + "#setEnergyCapacity(...)' before registering!")
        }
    }

    override fun getDisplayRecipes(): List<ItemStack> {
        val displayRecipes: MutableList<ItemStack> = ArrayList(production.size)

        for (prod in production) {
            val recipe = prod.recipe
            if (recipe.input.size != 1) {
                continue
            }

            displayRecipes.add(recipe.input[0])
        }

        return displayRecipes
    }

    override fun getInputSlots(): IntArray {
        return intArrayOf(10)
    }

    override fun getOutputSlots(): IntArray {
        return intArrayOf(13, 14, 15, 16, 17)
    }

    override fun getEnergyComponentType(): EnergyNetComponentType {
        return EnergyNetComponentType.CONSUMER
    }

    override fun getCapacity(): Int {
        return energyCapacity
    }

    /*
    fun addRecipe(seconds: Int, input: ItemStack, output: ItemStack) {
        registerRecipe(seconds, arrayOf(input), arrayOf(output))
    }

    fun registerRecipe(seconds: Int, input: ItemStack, output: ItemStack) {
        registerRecipe(MachineRecipe(seconds, arrayOf(input), arrayOf(output)))
    }

    fun registerRecipe(recipe: MachineRecipe) {
        Validate.isTrue(recipe.input.size == 1, "AItemGenerator must have only 1 input")
        Validate.isTrue(recipe.output.size == 1, "AItemGenerator must have only 1 output")

        recipe.ticks /= speed
        recipes.add(recipe)
    }

     */

    override fun preRegister() {
        addItemHandler(object : BlockTicker() {
            override fun tick(b: Block, sf: SlimefunItem, data: Config) {
                this@ItemGenerator.tick(b)
            }

            override fun isSynchronized(): Boolean {
                return false
            }
        })
    }

    private var initiated: Boolean = false
    private fun initLocation(b: Block) {
        if (initiated) return

        if (BlockStorage.hasBlockInfo(b)) {
            val locPosition = BlockStorage.getLocationInfo(b.location, KEY_POSITION) ?: "0"
            currentPosition = locPosition.toInt()

            val locConsumption = BlockStorage.getLocationInfo(b.location, KEY_CONSUMPTION) ?: production[0].energy.toString()
            currentConsumption = locConsumption.toInt()
        }

        initiated = true
    }

    private fun tick(b: Block) {
        initLocation(b)
        val inv = BlockStorage.getInventory(b)
        var currentOperation = processor.getOperation(b)

        if (currentOperation == null) {
            val next = findNextRecipe(inv) ?: return
            currentOperation = CraftingOperation(next)
            processor.startOperation(b, currentOperation)

            // Fixes #3534 - Update indicator immediately
            processor.updateProgressBar(inv, PROGRESS_SLOT, currentOperation)
            return
        }

        if (!takeCharge(b.location)) return

        if (!currentOperation.isFinished) {
            processor.updateProgressBar(inv, PROGRESS_SLOT, currentOperation)
            currentOperation.addProgress(1)
            return
        }

        inv.replaceExistingItem(PROGRESS_SLOT, CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, " "))
        for (output in currentOperation.results) {
            inv.pushItem(output.clone(), *outputSlots)
        }

        processor.endOperation(b)
    }

    /**
     * This method will remove charge from a location if it is chargeable.
     *
     * @param l
     * location to try to remove charge from
     * @return Whether charge was taken if its chargeable
     */
    private fun takeCharge(l: Location?): Boolean {
        Validate.notNull(l, "Can't attempt to take charge from a null location!")

        if (!isChargeable) {
            return true
        }

        val charge = getCharge(l!!)

        if (charge < energyConsumption) {
            return false
        }

        setCharge(l, charge - energyConsumption)
        return true
    }

    private fun findNextRecipe(inv: BlockMenu): MachineRecipe? {
        val slot = inputSlots[0]
        val item = inv.getItemInSlot(slot)

        val recipe = production[currentPosition].recipe


        val input = recipe.input[0]
        if (!SlimefunUtils.isItemSimilar(item, input, true)) {
            return null
        }

        if (!InvUtils.fitAll(inv.toInventory(), recipe.output, *outputSlots)) {
            return null
        }

        currentConsumption = production[currentPosition].energy
        return recipe
    }
}
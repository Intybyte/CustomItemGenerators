package me.vaan.customitemgen.generator

typealias Validator<T> = (T) -> Boolean

operator fun <K, T : Any> HashMap<K, Validator<*>>.get(key: K, arg: T) : Boolean {
    val validator = this[key] ?: return true
    val castedValidator = validator as Validator<T>
    return castedValidator.invoke(arg)
}
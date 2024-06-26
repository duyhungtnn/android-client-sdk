package io.bucketeer.sdk.android.internal.evaluation

import io.bucketeer.sdk.android.internal.logd
import io.bucketeer.sdk.android.internal.model.Evaluation
import org.json.JSONObject
import java.util.Locale
import kotlin.text.isEmpty

internal inline fun <reified T : Any> Evaluation?.getVariationValue(defaultValue: T): T {
  val value = this?.variationValue
  val typedValue: T =
    if (value != null) {
      @Suppress("IMPLICIT_CAST_TO_ANY")
      val anyValue =
        when (T::class) {
          String::class ->
            value
          Int::class -> value.toIntOrNull()
          Long::class -> value.toLongOrNull()
          Float::class -> value.toFloatOrNull()
          Double::class -> value.toDoubleOrNull()
          Boolean::class ->
            when (value.lowercase(Locale.ENGLISH)) {
              "true" -> true
              "false" -> false
              else -> null
            }
          JSONObject::class ->
            try {
              JSONObject(value)
            } catch (e: Exception) {
              null
            }
          else -> null
        }
      logd {
        if (anyValue == null) {
          "getVariation returns null reason: failed to cast"
        } else {
          null
        }
      }
      anyValue as? T ?: defaultValue
    } else {
      logd {
        "getVariation returns null reason: " +
          when {
            this == null -> {
              "Evaluation is null"
            }
            this.variationValue.isEmpty() -> {
              "Variation value is null or empty"
            }
            else -> {
              "Unknown"
            }
          }
      }
      defaultValue
    }
  return typedValue
}

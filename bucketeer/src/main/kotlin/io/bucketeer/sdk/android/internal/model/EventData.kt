package io.bucketeer.sdk.android.internal.model

import com.squareup.moshi.JsonClass

// we can't use codegen here
// see EventAdapterFactory
sealed class EventData {

  @JsonClass(generateAdapter = true)
  data class GoalEvent(
    val timestamp: Long,
    val goalId: String,
    val userId: String,
    val value: Double,
    val user: User,
    val tag: String,
    val sourceId: SourceID,
    val sdkVersion: String? = null,
    val metadata: Map<String, String>? = null,
  ) : EventData()

  @JsonClass(generateAdapter = true)
  data class EvaluationEvent(
    val timestamp: Long,
    val featureId: String,
    val feature_version: Int = 0,
    val userId: String,
    val variationId: String = "",
    val user: User,
    val reason: Reason,
    val tag: String,
    val sourceId: SourceID,
    val sdkVersion: String? = null,
    val metadata: Map<String, String>? = null,
  ) : EventData()

  // we can't use codegen here
  // see MetricsEventAdapterFactory
  data class MetricsEvent(
    val timestamp: Long,
    val event: MetricsEventData,
    val type: MetricsEventType,
    val sdkVersion: String? = null,
    val metadata: Map<String, String>? = null,
  ) : EventData()
}

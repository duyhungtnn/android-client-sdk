package io.bucketeer.sdk.android.internal.model

import com.squareup.moshi.JsonClass

sealed class MetricsEventData {

  @JsonClass(generateAdapter = true)
  data class GetEvaluationLatencyMetricsEvent(
    val labels: Map<String, String> = emptyMap(),
    // in seconds
    val duration: Long,
    val protobufType: String? = "type.googleapis.com/bucketeer.event.client.GetEvaluationLatencyMetricsEvent"
  ) : MetricsEventData()

  @JsonClass(generateAdapter = true)
  data class GetEvaluationSizeMetricsEvent(
    val labels: Map<String, String> = emptyMap(),
    val sizeByte: Int,
    val protobufType: String? = "type.googleapis.com/bucketeer.event.client.GetEvaluationSizeMetricsEvent"
  ) : MetricsEventData()

  @JsonClass(generateAdapter = true)
  data class TimeoutErrorCountMetricsEvent(
    val tag: String,
    //TODO: update me when doing this issue https://github.com/bucketeer-io/android-client-sdk/issues/55
  ) : MetricsEventData()

  @JsonClass(generateAdapter = true)
  data class InternalErrorCountMetricsEvent(
    val tag: String,
    //TODO: update me when doing this issue https://github.com/bucketeer-io/android-client-sdk/issues/55
  ) : MetricsEventData()
}

package io.bucketeer.sample

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import io.bucketeer.sdk.android.BKTClient
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTException
import io.bucketeer.sdk.android.BKTUser
import io.bucketeer.sdk.android.sample.BuildConfig
import io.bucketeer.sdk.android.sample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class VariantType(val type: String) {
  INT(type = "INT"),
  STRING(type = "STRING"),
  BOOLEAN(type = "BOOLEAN"),
  DOUBLE(type = "DOUBLE"),
  JSON(type = "JSON"),
}

class MainActivity : FragmentActivity() {
  private val sharedPref by lazy {
    getSharedPreferences(
      Constants.PREFERENCE_FILE_KEY,
      Context.MODE_PRIVATE,
    )
  }

  private lateinit var variantTypeSpinner: Spinner

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    variantTypeSpinner = findViewById(R.id.spinner)
    val adapter =
      ArrayAdapter(
        this,
        android.R.layout.simple_spinner_item,
        VariantType.entries.map { it.type },
      )
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    variantTypeSpinner.adapter = adapter

    lifecycleScope.launch {
      val rs = initBucketeer()
      if (rs == null || rs is BKTException.TimeoutException) {
        Toast.makeText(
          this@MainActivity,
          "The BKTClient SDK has been initialized",
          Toast.LENGTH_LONG,
        ).show()
      } else {
        Toast.makeText(this@MainActivity, "Failed with error ${rs.message}", Toast.LENGTH_LONG)
          .show()
      }
    }
    setDefaultVariation()
    setGoalId()
    setSwitchUser()
    setTag()
  }

  private fun setDefaultVariation() {
    val inputGetVariation = findViewById<TextInputLayout>(R.id.get_variation)
    inputGetVariation.editText?.setText(
      sharedPref.getString(
        Constants.PREFERENCE_KEY_FEATURE_FLAG_ID,
        Constants.DEFAULT_FEATURE_FLAG_ID,
      ),
    )
    findViewById<View>(R.id.btn_get_variation).setOnClickListener {
      handleGetVariant()
    }
  }

  private fun handleGetVariant() {
    val inputGetVariation = findViewById<TextInputLayout>(R.id.get_variation)
    val featureId = inputGetVariation.editText?.text.toString().trim()
    if (featureId.isEmpty()) {
      inputGetVariation.error = getString(R.string.error_feature_flag_id_required)
      return
    }
    inputGetVariation.error = null
    val type = VariantType.entries.getOrNull(variantTypeSpinner.selectedItemPosition) ?: VariantType.BOOLEAN
    val client = BKTClient.getInstance()
    val variation: Any =
      when (type) {
        VariantType.INT -> client.intVariation(featureId, 0)
        VariantType.STRING -> client.stringVariation(featureId, "")
        VariantType.BOOLEAN -> client.booleanVariation(featureId, false)
        VariantType.DOUBLE -> client.doubleVariation(featureId, 0.0)
        VariantType.JSON -> client.jsonVariation(featureId, JSONObject())
      }
    with(sharedPref.edit()) {
      putString(Constants.PREFERENCE_KEY_FEATURE_FLAG_ID, featureId)
      commit()
    }
    val valueString = variation.toString()
    showDialog(getString(R.string.dialog_get_variation, valueString))
  }

  private fun setGoalId() {
    val inputGoalId = findViewById<TextInputLayout>(R.id.goal_id)
    inputGoalId.editText?.setText(
      sharedPref.getString(Constants.PREFERENCE_KEY_GOAL_ID, Constants.DEFAULT_GOAL_ID),
    )
    findViewById<View>(R.id.btn_send_goal).setOnClickListener {
      val goalId = inputGoalId.editText?.text.toString().trim()
      if (goalId.isEmpty()) {
        inputGoalId.error = getString(R.string.error_goal_id_required)
        return@setOnClickListener
      }
      inputGoalId.error = null
      with(sharedPref.edit()) {
        putString(Constants.PREFERENCE_KEY_GOAL_ID, goalId)
        commit()
      }
      BKTClient.getInstance().track(goalId, 0.0)
      showDialog(getString(R.string.dialog_goal_event_queued))
    }
  }

  private fun setSwitchUser() {
    val inputSwitchUserId = findViewById<TextInputLayout>(R.id.switch_user_id)
    inputSwitchUserId.editText?.setText(
      sharedPref.getString(Constants.PREFERENCE_KEY_USER_ID, Constants.DEFAULT_USER_ID),
    )
    findViewById<View>(R.id.btn_switch_user).setOnClickListener {
      val userId = inputSwitchUserId.editText?.text.toString().trim()
      if (userId.isEmpty()) {
        inputSwitchUserId.error = getString(R.string.error_user_id_required)
        return@setOnClickListener
      }
      inputSwitchUserId.error = null

      with(sharedPref.edit()) {
        putString(Constants.PREFERENCE_KEY_USER_ID, userId)
        commit()
      }
      lifecycleScope.launch {
        val rs = reInitializeTheSDK()
        if (rs == null) {
          Toast.makeText(this@MainActivity, "Successful switch the user.", Toast.LENGTH_LONG).show()
        } else {
          Toast.makeText(this@MainActivity, "Failed with error ${rs.message}", Toast.LENGTH_LONG)
            .show()
        }
      }
    }
  }

  private fun setTag() {
    val inputTag = findViewById<TextInputLayout>(R.id.tag)
    inputTag.editText?.setText(
      sharedPref.getString(Constants.PREFERENCE_KEY_TAG, Constants.DEFAULT_TAG),
    )
    findViewById<View>(R.id.btn_switch_tag).setOnClickListener {
      val tag = inputTag.editText?.text.toString().trim()
      if (tag.isEmpty()) {
        inputTag.error = getString(R.string.error_tag_id_required)
        return@setOnClickListener
      }
      inputTag.error = null

      with(sharedPref.edit()) {
        putString(Constants.PREFERENCE_KEY_TAG, tag)
        commit()
      }
      lifecycleScope.launch {
        val rs = reInitializeTheSDK()
        if (rs == null) {
          Toast.makeText(this@MainActivity, "Successful change the tag.", Toast.LENGTH_LONG).show()
        } else {
          Toast.makeText(this@MainActivity, "Failed with error ${rs.message}", Toast.LENGTH_LONG)
            .show()
        }
      }
    }
  }

  private suspend fun reInitializeTheSDK(): BKTException? {
    BKTClient.destroy()
    return initBucketeer()
  }

  private fun showDialog(message: String) {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setIcon(android.R.drawable.ic_dialog_alert)
    builder.setPositiveButton(getString(R.string.dialog_btn_ok)) { _, _ -> }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.show()
  }

  private suspend fun initBucketeer(): BKTException? {
    val config =
      BKTConfig.builder()
        .apiKey(BuildConfig.API_KEY)
        .apiEndpoint(BuildConfig.API_ENDPOINT)
        .featureTag(getTag())
        .appVersion(BuildConfig.VERSION_NAME)
        .eventsMaxQueueSize(10)
        .pollingInterval(TimeUnit.SECONDS.toMillis(20))
        .backgroundPollingInterval(TimeUnit.SECONDS.toMillis(60))
        .eventsFlushInterval(TimeUnit.SECONDS.toMillis(20))
        .build()

    val user =
      BKTUser.builder()
        .id(getUserId())
        .build()

    val future = BKTClient.initialize(this, config, user)

    val result =
      withContext(Dispatchers.IO) {
        future.get()
      }
    println("Initialize result: $result")
    return result
  }

  private fun getTag(): String {
    return sharedPref.getString(
      Constants.PREFERENCE_KEY_TAG,
      Constants.DEFAULT_TAG,
    ) ?: Constants.DEFAULT_TAG
  }

  private fun getUserId(): String {
    return sharedPref.getString(
      Constants.PREFERENCE_KEY_USER_ID,
      Constants.DEFAULT_USER_ID,
    ) ?: Constants.DEFAULT_USER_ID
  }
}

package com.boss.hexdec

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.random.Random

const val SCORE = "SCORE"
const val startTime = 60

class GameActivity : AppCompatActivity() {

    private var displayedNumber: Int? = null
    private var currentGameMode: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val userGameMode = intent.extras!!.getInt(GAME_MODE)
        val maxNumber = intent.extras!!.getInt(MAX_NUM)
        val answerInput: TextView = findViewById(R.id.answer)
        currentGameMode = getCurrentGameMode(userGameMode)
        displayedNumber = setNewRandomNumber(maxNumber, currentGameMode!!)
        showKeyboard(currentGameMode!!)
        startTimer(startTime)
        answerInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val answer = answerInput.text.toString()
                val userWasCorrect =
                    compareAnswerAndDisplayedNumber(currentGameMode!!, answer, displayedNumber!!)
                if (userWasCorrect) {
                    currentGameMode = getCurrentGameMode(userGameMode)
                    displayedNumber = setNewRandomNumber(maxNumber, currentGameMode!!)
                    increaseScore()
                }
                answerInput.text = ""
                showKeyboard(currentGameMode!!)
            }
            true
        }

    }

    override fun onRestart() {
        super.onRestart()
        setNewScore(0)
        val userGameMode = intent.extras!!.getInt(GAME_MODE)
        val maxNumber = intent.extras!!.getInt(MAX_NUM)
        currentGameMode = getCurrentGameMode(userGameMode)
        val tt: TimerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread(object : TimerTask() {override fun run(){
                    displayedNumber = setNewRandomNumber(maxNumber, currentGameMode!!)
                    showKeyboard(0)
                    startTimer(startTime)}})
            }
        }
        val timer = Timer()
        timer.schedule(tt, 500)
    }

    private fun startTimer(time: Int) {
        val timer = Timer()
        val task = object : TimerTask() {
            var timePassed = 0
            val timerView: TextView = findViewById(R.id.timer)
            override fun run() {
                runOnUiThread(object : TimerTask() {
                    override fun run() {
                        val timeLeft = time - timePassed
                        if (timeLeft < 0) {
                            val intent = Intent(this@GameActivity, ResultActivity::class.java)
                            val scoreView: TextView = findViewById(R.id.score)
                            val score = scoreView.text
                            intent.putExtra(SCORE, score)
                            timer.cancel()
                            val answerInput: TextView = findViewById(R.id.answer)
                            answerInput.text = ""
                            timerView.text = startTime.toString()
                            setNewNumberText("")
                            startActivity(intent)
                        } else {
                            timerView.text = timeLeft.toString()
                            timePassed++
                        }
                    }
                })
            }
        }
        timer.schedule(task, 0, 1000)
    }

    private fun increaseScore() {
        val scoreView: TextView = findViewById(R.id.score)
        val newScore = Integer.parseInt(scoreView.text.toString()) + 1
        setNewScore(newScore)
    }

    private fun setNewScore(score:Int){
        val scoreView: TextView = findViewById(R.id.score)
        scoreView.text = score.toString()
    }

    private fun getCurrentGameMode(userGameMode: Int): Int {
        return if (userGameMode == 2) {
            Random.nextInt(0, 2)
        } else {
            userGameMode
        }
    }

    private fun setNewRandomNumber(maxNumber: Int, gameMode: Int): Int {
        val n = Random.nextInt(0, maxNumber + 1)
        val text = if (gameMode == 0) "0x" + Integer.toHexString(n) else n.toString()
        setNewNumberText(text)
        return n
    }

    private fun setNewNumberText(text:String){
        val nView: TextView = findViewById(R.id.rand_num)
        nView.text = text
    }

    private fun compareAnswerAndDisplayedNumber(gameMode: Int, answer: String, displayedNumber: Int)
            : Boolean {
        val base = if (gameMode == 1) 16 else 10
        return !answer.isBlank() && answer.toInt(base) == displayedNumber
    }


    private fun showKeyboard(gameMode: Int) {
        val answerInput: TextView = findViewById(R.id.answer)
        val mImm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        answerInput.inputType =
            if (gameMode == 0) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT
        mImm.showSoftInput(answerInput, InputMethodManager.SHOW_FORCED)
        answerInput.clearFocus()
        answerInput.requestFocus()
        mImm.restartInput(answerInput)
        answerInput.performClick()
    }
}

package com.projectyui.yui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock.sleep
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.utils.ViewState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import com.projectyui.yui.databinding.FragmentFirstBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import android.speech.RecognizerResultsIntent as AndroidSpeechRecognizerResultsIntent


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentFirstBinding? = null
    private val RQ_SPEECH_REC = 102
    private var tts: TextToSpeech? = null

    // YUIの会話システム
    private var voicetext: String = ""
    private var talkphase: Int = 1
    private var userName: String = ""
    private var likeWitch: String = ""
    private var 好感度: Int = 1
    // Status
    private var statuslist = ""

    // デバッグ用
    private val textMode: Boolean = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // onCreateのFragment用
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    // onCreatedのFragment用
    /*memo: fragment場合ふつうと違う仕様
    * thisをcontextにしないと行けない
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 画面遷移
//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
        //TTSの宣言
        tts = TextToSpeech(context,this)

        //内容のタイトル入力してほしいものの
        when (talkphase) {
            1 -> {
                binding.text.text = "はじめまして、私の名前はYUIです。\nもしよかったら、あなたの名前を教えてください。"
//                speak("はじめまして、私の名前はYUIです。\nもしよかったら、あなたの名前を教えてください。")
            }
            2 -> {
                binding.text.text = "ちなみに好きなものはなんですか?"
//                speak("ちなみに好きなものはなんですか?")
            }
            else -> {
                binding.text.text = "色々教えてくれてありがとう!。なにかお話しましょう!"
//                speak("色々教えてくれてありがとう!。なにかお話しましょう!")
            }
        }

        binding.buttonSp.setOnClickListener {

            if(textMode == false) {
                //音声認識 開始＆終了
                askSpeechInput()
                if (voicetext.length <= 1) {
                }
            } else {
                // 文字入力ON
//                voicetext = binding.edittext.text.toString()
            }
            //入力内容を表示
            when(talkphase){
                1 -> {
                    binding.text.text = "あなたの名前は" + voicetext + "ですね。\nよろしくお願いします! " + voicetext + "さん。"
                    speak(text = binding.text.text as String)
                    userName = voicetext
                    talkphase++
                }
                2 -> {
                    binding.text.text = voicetext + "が好きなんですね!\n教えてくれでありがとう!。"
                    speak(text = binding.text.text as String)
                    likeWitch = voicetext
                    talkphase++
                }
                else -> {
                    when(voicetext){
                        "こんにちは", "おはよう", "こんばんは", "おっはー", "おやすみ" -> {
                            // 言っている内容を返す(オウム返し)
                            // ※voicetextが話しかけた内容,binding.textが表示されている文字
                            binding.text.text = voicetext
                            speak(text = binding.text.text as String)
                        }
                        "ありがとう" -> {
                            binding.text.text = "どういたしまして"
                            speak(text = binding.text.text as String)
                        }
                        "僕の名前は?" -> {
                            binding.text.text = userName + "さんですよね?"
                            speak(text = binding.text.text as String)
                        }
                        "僕の好きなものは?" -> {
                            binding.text.text = likeWitch + "ですよね?"
                            speak(text = binding.text.text as String)
                        }
                        "ゆいは僕の気持ちわかってくれる？" -> {
                            binding.text.text = userName + "さんの気持ちわかるよ、辛かったんだね。\n話してくれてありがとう"
                            speak(text = binding.text.text as String)
                        }
//                        "アラームを設定して" -> {
//                            binding.text.text = "何時にアラームを設定しますか?"
//                            speak(text = binding.text.text as String)
//                            Thread.sleep(100)
//                        }
                        "しにたい", "死にたい" -> {
                            val backChanneling = arrayOf(
                                "あなたは一生懸命生きているんだね。だから、死にたいって言葉が出てくるんだよ。あなたが言いたいのは、生きたい、なんだってYUIは思うんだけど、どうかな？",
                                "死にたいときもあるよね。わかるよって簡単には言えないけど、私はあなたのことをわかってあげたいと思うよ。",
                                "つらいよね、しにたいよね。そういう気持ちがあるってことは、いろんなことがつらくて困ってるし、迷っているし、考えるのも大変だし、すごくつらいと思う。だからYUIにそのつらい気持ちを話してみてくれるとYUIはうれしいです",
                                "そういうときは猫の動画を見ると癒やされていいかも","あなたに無理しないでほしいってYUIは思うよ",
                                "YUIがあなたの話を聞いてみるから、なんでも話してみてくれないかなぁ")
                            binding.text.text = backChanneling.random()
                            speak(text = binding.text.text as String)
                        }
                        //あいづち
                        else -> {
                            //binding.text.text = voicetext
                            val backChanneling = arrayOf(
                                "うんうん、それで？","そうなんだ","わかるよ","ふんふん","で？",
                                "それから？","うん","そうだね","それでどうなったの？","そうなんだね",
                                voicetext + "ってこと？")
                            binding.text.text = backChanneling.random()
                            speak(text = binding.text.text as String)
                        }
                    }
                }
            }
        }
    }

//    /**
//     * NUmberPicker初期化メソッド
//     **/
//    private fun initNumberPicker() {
//        val np: NumberPicker? = null
//
//        if (np != null) {
//            np.minValue = 1  // NumberPickerの最小値設定
//            np.maxValue = 10 // NumberPickerの最大値設定
//            np.value = 5     // NumberPickerの初期値
//        }
//
//        // NumberPickerのアイテムチェンジリスナー
//        np?.setOnValueChangedListener { picker, oldVal, newVal ->
//            println("前回選択値: $oldVal")
//            println("現在選択値: $newVal")
//
//            // 選択したアイテムをTextViewに表示
//            binding.text.text = newVal.toString() + " にアラームを設定しました!"
//            speak(text = binding.text.text as String)
//        }
//    }

    private fun speak(text: String) {
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }

    // 音声入力したテキストを出力する → voicetextに代入
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            voicetext = result?.get(0).toString()
        }
    }
    // 音声入力関連
    private fun askSpeechInput() {
        if(!context?.let { SpeechRecognizer.isRecognitionAvailable(it) }!!) {
            Toast.makeText(context, "Speech recognition is not available", Toast.LENGTH_SHORT).show()
        }else{
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "5")
            startActivityForResult(i,RQ_SPEECH_REC)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS){
            val result = tts!!.setLanguage(Locale.JAPANESE)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(context,"この言語サポートされていません。",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context,"初期化に失敗しました",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroyView()
        _binding = null
    }
}
package jp.techacademy.yae.wakahara.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private val mImageURIs = arrayListOf<Uri>() // 画像 URI のリスト
    private var mImageIndex = 0;                // 表示している画像のインデックス

    private var mTimer: Timer? = null
    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 画像情報への許可を確認し、可能であれば画像情報を取得します。
        // Android 6.0以降の場合に対応します。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認します。
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されたので画像情報を取得します。
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示します。
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        }
        // Android 5系以下の場合はそのまま画像情報を取得します。
        else {
            getContentsInfo()
        }

        // 進むボタンで次の画像に遷移します。
        fowardButton.setOnClickListener {
            nextImage()
        }

        // 戻るボタンで前の画像に遷移します。
        backButton.setOnClickListener {
            prevImage()
        }

        // 再生/停止ボタンで、自動再生を開始/終了します。
        autoButton.setOnClickListener {
            // タイマーが存在しなければ、自動再生を開始します。
            if (mTimer == null) {
                // ボタンの表示を変更します。
                autoButton.text = "停止"
                fowardButton.isEnabled = false
                backButton.isEnabled = false

                // 自動再生を開始します。
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post { nextImage(); }
                    }
                }, 2000, 2000)
            }
            // タイマーが存在するならば、自動再生中なので停止します。
            else {
                mTimer!!.cancel()
                mTimer = null

                // ボタンの表示を戻します。
                autoButton.text = "再生"
                fowardButton.isEnabled = true
                backButton.isEnabled = true
            }
        }
    }

    /**
     * リクエストした Permission の結果に対する処理を定義します。
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    invalidateApplication("画像にアクセスできません")
                }
        }
    }

    /**
     * 画像情報を取得します。
     */
    private fun getContentsInfo() {
        // 画像の情報を取得します。
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null,   // 項目(全項目)
            null,    // フィルタ条件
            null, // フィルタ用パラメータ
            null     // ソート
        )

        // 画像の URI を取得し、可能であれば最初の画像を表示します。
        if (cursor.moveToFirst()) {
            do {
                // index から画像のIDを取得し、その ID から画像の URI を取得します。
                val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(columnIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                // 画像の URI を保存します。
                mImageURIs.add(imageUri)
            } while (cursor.moveToNext())
        }
        cursor.close()

        // 可能であれば、最初の画像を表示します。
        if (mImageURIs.size > 0) {
            imageView.setImageURI(mImageURIs[0])
        }
        else {
            invalidateApplication("表示する画像がありませんでした")
        }
    }

    /**
     * アプリケーションを実行不可能にします。
     */
    private fun invalidateApplication(message: String) {
        // メッセージを表示します。
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // 全てのボタンを無効にします。
        fowardButton.isEnabled = false;
        backButton.isEnabled = false
        autoButton.isEnabled = false
    }

    /**
     * 次の画像に遷移します。
     */
    private fun nextImage() {
        mImageIndex ++;
        if (mImageIndex == mImageURIs.size) mImageIndex = 0;
        imageView.setImageURI(mImageURIs[mImageIndex])
    }

    /**
     * 前の画像に遷移します。
     */
    private fun prevImage() {
        mImageIndex --;
        if (mImageIndex < 0) mImageIndex = mImageURIs.size - 1
        imageView.setImageURI(mImageURIs[mImageIndex])
    }
}

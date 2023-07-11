package lec.baekseokuniv.ssiholder.app

import android.app.Application
import android.content.Context
import android.system.Os
import lec.baekseokuniv.ssiholder.config.PoolConfig
import lec.baekseokuniv.ssiholder.config.WalletConfig
import org.hyperledger.indy.sdk.wallet.Wallet

class App : Application() {
    private val PREFERENCE_FILE_MY_WALLET = "PREFERENCE_FILE_MY_WALLET"
    private val PREF_MASTER_SECRET_ID = "PREF_MASTER_SECRET_ID"
    private val PREF_KEY_DID = "PREF_KEY_DID"
    private val PREF_KEY_VERKEY = "PREF_KEY_VERKEY"

    lateinit var wallet: Wallet

    override fun onCreate() {
        super.onCreate()

        //TODO
        // 1. type = null 인데, 이 부분 확인
        // 2. manifest 등에서 설정할 수 있는지 여부 확인
        //지갑 설정 과정
        //1. load indy library
        Os.setenv("EXTERNAL_STORAGE", dataDir.absolutePath, true)
        System.loadLibrary("indy")

        //2. create pool
        PoolConfig.createPool()

        //3. create and then open wallet
        wallet = WalletConfig.openWallet()

        //4. create secret
        WalletConfig.createMasterSecretId(wallet, getMasterSecretId())
            .apply {
                getSharedPreferences(PREFERENCE_FILE_MY_WALLET, Context.MODE_PRIVATE)
                    .also {
                        with(it.edit()) {
                            putString(PREF_MASTER_SECRET_ID, this@apply)
                            apply()
                        }
                    }
            }

        //5. create DID
        if (getDid().isNullOrEmpty() || getVerKey().isNullOrEmpty()) {
            WalletConfig.createDid(wallet)
                .thenAccept { didAndVerKey ->
                    getSharedPreferences(PREFERENCE_FILE_MY_WALLET, Context.MODE_PRIVATE)
                        .also {
                            with(it.edit()) {
                                putString(PREF_KEY_DID, didAndVerKey.first)
                                putString(PREF_KEY_VERKEY, didAndVerKey.second)
                                apply()
                            }
                        }
                }
        }
    }

    fun getMasterSecretId(): String? {
        return getSharedPreferences(PREFERENCE_FILE_MY_WALLET, Context.MODE_PRIVATE)
            .getString(PREF_MASTER_SECRET_ID, null)
    }

    fun getDid(): String? {
        return getSharedPreferences(PREFERENCE_FILE_MY_WALLET, Context.MODE_PRIVATE)
            .getString(PREF_KEY_DID, "")
    }

    fun getVerKey(): String? {
        return getSharedPreferences(PREFERENCE_FILE_MY_WALLET, Context.MODE_PRIVATE)
            .getString(PREF_KEY_VERKEY, "")
    }
}
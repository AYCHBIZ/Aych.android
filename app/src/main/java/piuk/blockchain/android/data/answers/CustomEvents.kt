package piuk.blockchain.android.data.answers

import com.crashlytics.android.answers.CustomEvent
import piuk.blockchain.android.util.extensions.getAmountRange
import java.math.BigInteger

class RecoverWalletEvent : CustomEvent("Recover Wallet") {

    fun putSuccess(successful: Boolean): RecoverWalletEvent {
        putCustomAttribute("Success", if (successful) "true" else "false")
        return this
    }

}

class PairingEvent : CustomEvent("Wallet Pairing") {

    fun putSuccess(successful: Boolean): PairingEvent {
        putCustomAttribute("Success", if (successful) "true" else "false")
        return this
    }

    fun putMethod(pairingMethod: PairingMethod): PairingEvent {
        putCustomAttribute("Pairing method", pairingMethod.name)
        return this
    }

}

@Suppress("UNUSED_PARAMETER")
enum class PairingMethod(name: String) {
    MANUAL("Manual"),
    QR_CODE("Qr code"),
    REVERSE("Reverse")
}

class PaymentSentEvent : CustomEvent("Payment Sent") {

    fun putSuccess(successful: Boolean): PaymentSentEvent {
        putCustomAttribute("Success", if (successful) "true" else "false")
        return this
    }

    fun putAmountForRange(amountSent: BigInteger): PaymentSentEvent {
        putCustomAttribute("Amount", amountSent.getAmountRange())
        return this
    }

}

class ImportEvent(addressType: AddressType) : CustomEvent("Address Imported") {

    init {
        putCustomAttribute("Address Type", addressType.name)
    }

}

@Suppress("UNUSED_PARAMETER")
enum class AddressType(name: String) {
    PRIVATE_KEY("Private key"),
    WATCH_ONLY("Watch Only")
}

class CreateAccountEvent(number: Int) : CustomEvent("Account Created") {

    init {
        putCustomAttribute("Number of Accounts", number)
    }

}


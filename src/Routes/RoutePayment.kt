package com.example.Routes

import com.example.Models.CreditCard
import com.google.gson.Gson
import com.stripe.Stripe
import com.stripe.model.PaymentIntent
import com.stripe.model.PaymentMethod
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.routing.post
import io.ktor.routing.routing
import java.util.ArrayList
import java.util.HashMap

fun setEnv(){

    val appConfig = HoconApplicationConfig(ConfigFactory.load())

    Stripe.apiKey =  appConfig.property("ktor.env.key").getString()

}

fun TransformJSONToArray(request: String): Array<CreditCard> {
    return Gson().fromJson(request, Array<CreditCard>::class.java)
}

fun SpecifyContentCard(content: Array<CreditCard>): CreditCard{

    val name = content[0].name
    val number = content[0].number
    val cvc = content[0].cvc
    val expiry: String = content[0].expiry

    return CreditCard(number, name, cvc, expiry)

}

fun SplitExpiry(expiry: String): List<String>{
    return expiry.split("/")
}

fun CreatePaymentMethod(ContentCard: CreditCard): PaymentMethod? {

    val card = HashMap<String, Any>()
    card["number"] = ContentCard.number
    card["exp_month"] = SplitExpiry(ContentCard.expiry)[0]
    card["exp_year"] = SplitExpiry(ContentCard.expiry)[1]
    card["cvc"] = ContentCard.cvc

    val params = HashMap<String, Any>()
    params["type"] = "card"
    params["card"] = card

    return PaymentMethod.create(params)

}


fun CreatePaymentIntent(): PaymentIntent? {

    val paymentMethodTypes = ArrayList<Any>()
    paymentMethodTypes.add("card")

    val params = HashMap<String, Any>()
    params["currency"] = "brl"
    params["amount"] = 2000

    params["payment_method_types"] = paymentMethodTypes

    return PaymentIntent.create(params)

}

fun Application.RoutePayment() {

    routing {

        post("/pay") {

            val request = call.receive<String>()

            val TransformedJSONToArray = TransformJSONToArray(request)

            val SpecifiedContentCard = SpecifyContentCard(TransformedJSONToArray)

            setEnv()

            val paymentMethod = CreatePaymentMethod(SpecifiedContentCard)

            val paymentIntent = CreatePaymentIntent()

            try{

                val params = HashMap<String, Any>()

                if (paymentMethod != null) {
                    params["payment_method"] = paymentMethod.id
                }
                if (paymentIntent != null) {
                    paymentIntent.confirm(params)
                }

                call.response.status(HttpStatusCode.OK)

            }catch(e: NullPointerException){
                call.response.status(HttpStatusCode.FailedDependency)
            }

        }
    }
}


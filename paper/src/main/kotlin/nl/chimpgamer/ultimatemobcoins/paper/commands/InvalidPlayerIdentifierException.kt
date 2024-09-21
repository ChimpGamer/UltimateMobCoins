package nl.chimpgamer.ultimatemobcoins.paper.commands

class InvalidPlayerIdentifierException(message: String) : IllegalArgumentException(message) {

    override fun fillInStackTrace(): Throwable {
        return this
    }
}
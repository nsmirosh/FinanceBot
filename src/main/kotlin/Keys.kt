package nick.mirosh

val BOT_TOKEN = System.getenv("BOT_TOKEN") ?: throw IllegalArgumentException("BOT_TOKEN must be set")
val MONGO_DB_CONNECTION_STRING = System.getenv("MONGO_DB_CONNECTION_STRING")
    ?: throw IllegalArgumentException("mongo_db_connection_string must be set")
val DATABASE_NAME = System.getenv("DATABASE_NAME") ?: throw IllegalArgumentException("database_name must be set")




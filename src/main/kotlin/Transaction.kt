package nick.mirosh
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Transaction(
    @BsonId val id: ObjectId,
    val utcDate: Int,
    val sum: Int,
    val currency: String,
    val category: String,
    val description: String = "",
    val userName: String? = ""
)

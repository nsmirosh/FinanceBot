package nick.mirosh
import nick.mirosh.utils.Category
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Transaction(
    @BsonId val id: ObjectId,
    val utcDate: Int,
    val sum: Int,
    val currency: String,
    val category: Category,
    val description: String = "",
    val userName: String? = ""
)

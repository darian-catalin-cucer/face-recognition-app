package brain_factory.face_recognition

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import org.bson.types.ObjectId
import java.nio.FloatBuffer

open class Person(
    newFirstName: String = "John",
    newLastName: String = "Doe",
    newEmail: String = "john.doe@gmail.com",
    newPhone: String = "+4070000000",
    newPosition: String = "Unknown",
    newEmbeddings: FloatBuffer = FloatBuffer.allocate(512),
    project: String = "FaceRecognition"
) : RealmObject()
{
    @PrimaryKey
    var _id: ObjectId = ObjectId()

    @Required
    var firstName: String = newFirstName

    @Required
    var lastName: String = newLastName

    @Required
    var email: String = newEmail

    @Required
    var phone: String = newPhone

    @Required
    var position: String = newPosition

    @Required
    var embeddings: RealmList<Float> = Utils.realmListFromFloatBuffer(newEmbeddings)

    override fun toString(): String
    {
        return "$firstName $lastName: $email"
    }
}
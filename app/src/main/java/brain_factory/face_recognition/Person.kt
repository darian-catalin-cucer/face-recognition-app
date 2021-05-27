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

    var embeddings: RealmList<Float> = floatBufferToRealmList(newEmbeddings)

    @Required
    var email: String = newEmail
    @Required
    var phone: String = newPhone
    @Required
    var firstName: String = newFirstName
    @Required
    var lastName: String = newLastName
    @Required
    var position: String = newPosition

    companion object
    {
        fun floatBufferToRealmList(buffer: FloatBuffer): RealmList<Float>
        {
            val result: RealmList<Float> = RealmList()
            for (i in 0 .. 511)
            {
                result.add(buffer[i])
            }
            return result
        }
    }
}
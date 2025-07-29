package com.mobdeve.s17.mco2.group88

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

// Updated User data class with security question fields
data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String = "", // Store hashed password
    val age: Int,
    val weight: Double, // in kg
    val height: Double, // in cm
    val sex: String, // "male" or "female"
    val dailyWaterGoal: Int, // in ml
    val notificationFrequency: Int, // in minutes
    val securityQuestion: String = "", // Security question
    val securityAnswerHash: String = "", // Now stores plain text security answer
    val createdAt: String = getCurrentDateTime(),
    val updatedAt: String = getCurrentDateTime(),
    val googleId: String? = null, // for google
    val facebookId: String? = null
)

data class WaterIntake(
    val id: Long = 0,
    val userId: Long,
    val amount: Int, // in ml
    val date: String, // YYYY-MM-DD format
    val time: String, // HH:mm:ss format
    val createdAt: String = getCurrentDateTime()
)

data class DailyIntakeSummary(
    val date: String,
    val totalIntake: Int,
    val goalAchieved: Boolean
)

// Helper function to get current date time
fun getCurrentDateTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

// Password hashing utility (keep this for passwords)
fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun verifyPassword(password: String, hash: String): Boolean {
    return hashPassword(password) == hash
}

class AquaBuddyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "AquaBuddy.db"
        private const val DATABASE_VERSION = 7 // Increment version to force upgrade

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD_HASH = "password_hash"
        private const val COLUMN_USER_AGE = "age"
        private const val COLUMN_USER_WEIGHT = "weight"
        private const val COLUMN_USER_HEIGHT = "height"
        private const val COLUMN_USER_SEX = "sex"
        private const val COLUMN_USER_DAILY_GOAL = "daily_water_goal"
        private const val COLUMN_USER_NOTIFICATION_FREQ = "notification_frequency"
        private const val COLUMN_USER_SECURITY_QUESTION = "security_question"
        private const val COLUMN_USER_SECURITY_ANSWER_HASH = "security_answer_hash"
        private const val COLUMN_USER_CREATED_AT = "created_at"
        private const val COLUMN_USER_UPDATED_AT = "updated_at"
        private const val COLUMN_USER_GOOGLE_ID = "google_id"
        private const val COLUMN_USER_FACEBOOK_ID = "facebook_id"

        // Water intake table
        private const val TABLE_WATER_INTAKE = "water_intake"
        private const val COLUMN_INTAKE_ID = "id"
        private const val COLUMN_INTAKE_USER_ID = "user_id"
        private const val COLUMN_INTAKE_AMOUNT = "amount"
        private const val COLUMN_INTAKE_DATE = "date"
        private const val COLUMN_INTAKE_TIME = "time"
        private const val COLUMN_INTAKE_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DatabaseHelper", "Creating database tables...")

        // Create users table with security question fields
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD_HASH TEXT NOT NULL,
                $COLUMN_USER_AGE INTEGER NOT NULL,
                $COLUMN_USER_WEIGHT REAL NOT NULL,
                $COLUMN_USER_HEIGHT REAL NOT NULL,
                $COLUMN_USER_SEX TEXT NOT NULL,
                $COLUMN_USER_DAILY_GOAL INTEGER NOT NULL,
                $COLUMN_USER_NOTIFICATION_FREQ INTEGER DEFAULT 60,
                $COLUMN_USER_SECURITY_QUESTION TEXT NOT NULL DEFAULT '',
                $COLUMN_USER_SECURITY_ANSWER_HASH TEXT NOT NULL DEFAULT '',
                $COLUMN_USER_CREATED_AT TEXT NOT NULL,
                $COLUMN_USER_UPDATED_AT TEXT NOT NULL,
                $COLUMN_USER_GOOGLE_ID TEXT,
                $COLUMN_USER_FACEBOOK_ID TEXT
            )
        """.trimIndent()

        // Create water intake table
        val createWaterIntakeTable = """
            CREATE TABLE $TABLE_WATER_INTAKE (
                $COLUMN_INTAKE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_INTAKE_USER_ID INTEGER NOT NULL,
                $COLUMN_INTAKE_AMOUNT INTEGER NOT NULL,
                $COLUMN_INTAKE_DATE TEXT NOT NULL,
                $COLUMN_INTAKE_TIME TEXT NOT NULL,
                $COLUMN_INTAKE_CREATED_AT TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_INTAKE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        try {
            db.execSQL(createUsersTable)
            db.execSQL(createWaterIntakeTable)

            // Create indexes for better performance
            db.execSQL("CREATE INDEX idx_water_intake_user_date ON $TABLE_WATER_INTAKE($COLUMN_INTAKE_USER_ID, $COLUMN_INTAKE_DATE)")
            db.execSQL("CREATE INDEX idx_users_email ON $TABLE_USERS($COLUMN_USER_EMAIL)")

            Log.d("DatabaseHelper", "Database tables created successfully")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error creating database tables: ${e.message}", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("DatabaseHelper", "Upgrading database from version $oldVersion to $newVersion")

        if (oldVersion < 2) {
            // Add password column to existing users table
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_PASSWORD_HASH TEXT DEFAULT ''")
                Log.d("DatabaseHelper", "Added password_hash column")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Error adding password_hash column: ${e.message}", e)
            }
        }
        if (oldVersion < 3) {
            // Add google_id column
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_GOOGLE_ID TEXT")
                Log.d("DatabaseHelper", "Added google_id column")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Error adding google_id column: ${e.message}", e)
            }
        }
        if (oldVersion < 4) {
            // Add facebook_id column
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_FACEBOOK_ID TEXT")
                Log.d("DatabaseHelper", "Added facebook_id column")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Error adding facebook_id column: ${e.message}", e)
            }
        }
        if (oldVersion < 5) {
            // Add security question and answer columns
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_SECURITY_QUESTION TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_SECURITY_ANSWER_HASH TEXT DEFAULT ''")
                Log.d("DatabaseHelper", "Added security question and answer columns")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Error adding security columns: ${e.message}", e)
            }
        }
        if (oldVersion < 6) {
            // Version 6 - just for better error handling, no schema changes
            Log.d("DatabaseHelper", "Upgraded to version 6 for improved error handling")
        }
        if (oldVersion < 7) {
            // Ensure security columns exist (safety check)
            try {
                // Check if columns exist, if not add them
                val cursor = db.rawQuery("PRAGMA table_info($TABLE_USERS)", null)
                val columnNames = mutableSetOf<String>()

                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    columnNames.add(columnName)
                }
                cursor.close()

                if (!columnNames.contains(COLUMN_USER_SECURITY_QUESTION)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_SECURITY_QUESTION TEXT DEFAULT ''")
                    Log.d("DatabaseHelper", "Added missing security_question column")
                }

                if (!columnNames.contains(COLUMN_USER_SECURITY_ANSWER_HASH)) {
                    db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_SECURITY_ANSWER_HASH TEXT DEFAULT ''")
                    Log.d("DatabaseHelper", "Added missing security_answer_hash column")
                }

                Log.d("DatabaseHelper", "Upgraded to version 7 - ensured security columns exist")
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Error in version 7 upgrade: ${e.message}", e)
            }
        }
    }

    // Helper function to check if security columns exist
    private fun checkSecurityColumnsExist(db: SQLiteDatabase): Boolean {
        return try {
            val cursor = db.rawQuery("PRAGMA table_info($TABLE_USERS)", null)
            val columnNames = mutableSetOf<String>()

            while (cursor.moveToNext()) {
                val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                columnNames.add(columnName)
            }
            cursor.close()

            val hasSecurityQuestion = columnNames.contains(COLUMN_USER_SECURITY_QUESTION)
            val hasSecurityAnswer = columnNames.contains(COLUMN_USER_SECURITY_ANSWER_HASH)

            Log.d("DatabaseHelper", "Security question column exists: $hasSecurityQuestion")
            Log.d("DatabaseHelper", "Security answer column exists: $hasSecurityAnswer")

            hasSecurityQuestion && hasSecurityAnswer
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error checking column existence: ${e.message}", e)
            false
        }
    }

    // Enhanced User management functions with better error handling
    fun insertUser(user: User): Long {
        val db = this.writableDatabase

        try {
            // Validate required fields
            if (user.name.isBlank()) {
                Log.e("DatabaseHelper", "Cannot insert user: name is blank")
                return -1L
            }
            if (user.email.isBlank()) {
                Log.e("DatabaseHelper", "Cannot insert user: email is blank")
                return -1L
            }

            // Check if security columns exist
            val securityColumnsExist = checkSecurityColumnsExist(db)

            val values = ContentValues().apply {
                put(COLUMN_USER_NAME, user.name.trim())
                put(COLUMN_USER_EMAIL, user.email.trim().lowercase())
                put(COLUMN_USER_PASSWORD_HASH, user.passwordHash)
                put(COLUMN_USER_AGE, user.age)
                put(COLUMN_USER_WEIGHT, user.weight)
                put(COLUMN_USER_HEIGHT, user.height)
                put(COLUMN_USER_SEX, user.sex.lowercase())
                put(COLUMN_USER_DAILY_GOAL, user.dailyWaterGoal)
                put(COLUMN_USER_NOTIFICATION_FREQ, user.notificationFrequency)
                put(COLUMN_USER_CREATED_AT, user.createdAt)
                put(COLUMN_USER_UPDATED_AT, user.updatedAt)
                put(COLUMN_USER_GOOGLE_ID, user.googleId)
                put(COLUMN_USER_FACEBOOK_ID, user.facebookId)

                // Only add security columns if they exist in the database
                if (securityColumnsExist) {
                    put(COLUMN_USER_SECURITY_QUESTION, user.securityQuestion)
                    put(COLUMN_USER_SECURITY_ANSWER_HASH, user.securityAnswerHash)
                    Log.d("DatabaseHelper", "Including security columns in insert")
                } else {
                    Log.w("DatabaseHelper", "Security columns don't exist, skipping them")
                }
            }

            // Log what we're trying to insert
            Log.d("DatabaseHelper", "Attempting to insert user:")
            Log.d("DatabaseHelper", "- Name: '${user.name}'")
            Log.d("DatabaseHelper", "- Email: '${user.email}'")
            Log.d("DatabaseHelper", "- Age: ${user.age}")
            Log.d("DatabaseHelper", "- Weight: ${user.weight}")
            Log.d("DatabaseHelper", "- Height: ${user.height}")
            Log.d("DatabaseHelper", "- Sex: '${user.sex}'")
            Log.d("DatabaseHelper", "- Daily Goal: ${user.dailyWaterGoal}")
            Log.d("DatabaseHelper", "- Security Question: '${user.securityQuestion}'")
            Log.d("DatabaseHelper", "- Has Security Answer: ${user.securityAnswerHash.isNotEmpty()}")
            Log.d("DatabaseHelper", "- Security columns exist: $securityColumnsExist")

            val result = db.insert(TABLE_USERS, null, values)

            if (result == -1L) {
                Log.e("DatabaseHelper", "Insert failed - db.insert returned -1")

                // Check if it's a constraint violation (duplicate email)
                val existingUser = getUserByEmail(user.email)
                if (existingUser != null) {
                    Log.e("DatabaseHelper", "Insert failed due to duplicate email: ${user.email}")
                } else {
                    Log.e("DatabaseHelper", "Insert failed for unknown reason")
                }
            } else {
                Log.d("DatabaseHelper", "Insert successful, user ID: $result")

                // If security columns don't exist but we have security data, try to update them later
                if (!securityColumnsExist && (user.securityQuestion.isNotEmpty() || user.securityAnswerHash.isNotEmpty())) {
                    Log.d("DatabaseHelper", "Attempting to add security data after initial insert")
                    updateSecurityQuestion(result, user.securityQuestion, user.securityAnswerHash)
                }
            }

            return result

        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception in insertUser: ${e.message}", e)
            return -1L
        }
    }

    fun getUserByEmail(email: String): User? {
        if (email.isBlank()) {
            Log.w("DatabaseHelper", "getUserByEmail called with blank email")
            return null
        }

        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.query(
                TABLE_USERS,
                null,
                "$COLUMN_USER_EMAIL = ?",
                arrayOf(email.trim().lowercase()),
                null,
                null,
                null
            )

            return if (cursor.moveToFirst()) {
                val user = cursorToUser(cursor)
                Log.d("DatabaseHelper", "Found user with email: $email")
                user
            } else {
                Log.d("DatabaseHelper", "No user found with email: $email")
                null
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception in getUserByEmail: ${e.message}", e)
            return null
        } finally {
            cursor?.close()
        }
    }

    // Create Google Users (updated to include empty security fields)
    fun createGoogleUser(name: String, email: String, googleId: String): Long {
        if (name.isBlank() || email.isBlank() || googleId.isBlank()) {
            Log.e("DatabaseHelper", "Cannot create Google user: missing required fields")
            return -1L
        }

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, name.trim())
            put(COLUMN_USER_EMAIL, email.trim().lowercase())
            put(COLUMN_USER_PASSWORD_HASH, "") // Empty for Google users
            put(COLUMN_USER_AGE, 0) // Use 0 to indicate not set
            put(COLUMN_USER_WEIGHT, 0.0)
            put(COLUMN_USER_HEIGHT, 0.0)
            put(COLUMN_USER_SEX, "not_specified")
            put(COLUMN_USER_DAILY_GOAL, 2150) // Reasonable default
            put(COLUMN_USER_NOTIFICATION_FREQ, 60)
            put(COLUMN_USER_SECURITY_QUESTION, "") // Empty for social login users
            put(COLUMN_USER_SECURITY_ANSWER_HASH, "") // Empty for social login users
            put(COLUMN_USER_CREATED_AT, getCurrentDateTime())
            put(COLUMN_USER_UPDATED_AT, getCurrentDateTime())
            put(COLUMN_USER_GOOGLE_ID, googleId)
        }

        return db.insert(TABLE_USERS, null, values)
    }

    // Create Facebook Users (updated to include empty security fields)
    fun createFacebookUser(name: String, email: String, facebookId: String): Long {
        if (name.isBlank() || email.isBlank() || facebookId.isBlank()) {
            Log.e("DatabaseHelper", "Cannot create Facebook user: missing required fields")
            return -1L
        }

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, name.trim())
            put(COLUMN_USER_EMAIL, email.trim().lowercase())
            put(COLUMN_USER_PASSWORD_HASH, "") // Empty for Facebook users
            put(COLUMN_USER_AGE, 0) // Use 0 to indicate not set
            put(COLUMN_USER_WEIGHT, 0.0)
            put(COLUMN_USER_HEIGHT, 0.0)
            put(COLUMN_USER_SEX, "not_specified")
            put(COLUMN_USER_DAILY_GOAL, 2150) // Reasonable default
            put(COLUMN_USER_NOTIFICATION_FREQ, 60)
            put(COLUMN_USER_SECURITY_QUESTION, "") // Empty for social login users
            put(COLUMN_USER_SECURITY_ANSWER_HASH, "") // Empty for social login users
            put(COLUMN_USER_CREATED_AT, getCurrentDateTime())
            put(COLUMN_USER_UPDATED_AT, getCurrentDateTime())
            put(COLUMN_USER_FACEBOOK_ID, facebookId)
        }

        return db.insert(TABLE_USERS, null, values)
    }

    // Authentication function
    fun authenticateUser(email: String, password: String): User? {
        val user = getUserByEmail(email)
        return if (user != null && verifyPassword(password, user.passwordHash)) {
            Log.d("DatabaseHelper", "User authentication successful for: $email")
            user
        } else {
            Log.d("DatabaseHelper", "User authentication failed for: $email")
            null
        }
    }

    // Function for password recovery using security question - now uses plain text comparison
    fun verifySecurityQuestionAnswer(email: String, answer: String): Boolean {
        val user = getUserByEmail(email)
        return if (user != null && user.securityAnswerHash.isNotEmpty()) {
            // Simple case-insensitive comparison instead of hash verification
            val result = answer.trim().lowercase() == user.securityAnswerHash.trim().lowercase()
            Log.d("DatabaseHelper", "Security question verification for $email: $result")
            result
        } else {
            Log.d("DatabaseHelper", "Security question verification failed for $email: no user or no security answer")
            false
        }
    }

    // Function to get security question by email
    fun getSecurityQuestionByEmail(email: String): String? {
        val user = getUserByEmail(email)
        return if (user != null && user.securityQuestion.isNotEmpty()) {
            user.securityQuestion
        } else {
            null
        }
    }

    // Function to update password after security question verification
    fun updatePasswordWithSecurityVerification(email: String, securityAnswer: String, newPassword: String): Boolean {
        if (!verifySecurityQuestionAnswer(email, securityAnswer)) {
            return false
        }

        val user = getUserByEmail(email) ?: return false
        val hashedNewPassword = hashPassword(newPassword)

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_PASSWORD_HASH, hashedNewPassword)
            put(COLUMN_USER_UPDATED_AT, getCurrentDateTime())
        }

        val rowsUpdated = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email.trim().lowercase())
        )

        return rowsUpdated > 0
    }

    // Function to update security question and answer - now stores plain text
    fun updateSecurityQuestion(userId: Long, securityQuestion: String, securityAnswer: String): Boolean {
        val db = this.writableDatabase

        try {
            val values = ContentValues().apply {
                put(COLUMN_USER_SECURITY_QUESTION, securityQuestion)
                put(COLUMN_USER_SECURITY_ANSWER_HASH, securityAnswer) // Store plain text
                put(COLUMN_USER_UPDATED_AT, getCurrentDateTime())
            }

            val rowsUpdated = db.update(
                TABLE_USERS,
                values,
                "$COLUMN_USER_ID = ?",
                arrayOf(userId.toString())
            )

            Log.d("DatabaseHelper", "Updated security question for user $userId: ${rowsUpdated > 0}")
            return rowsUpdated > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error updating security question: ${e.message}", e)
            return false
        }
    }

    fun getUserById(userId: Long): User? {
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.query(
                TABLE_USERS,
                null,
                "$COLUMN_USER_ID = ?",
                arrayOf(userId.toString()),
                null,
                null,
                null
            )

            return if (cursor.moveToFirst()) {
                cursorToUser(cursor)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception in getUserById: ${e.message}", e)
            return null
        } finally {
            cursor?.close()
        }
    }

    fun updateUser(user: User): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, user.name.trim())
            put(COLUMN_USER_EMAIL, user.email.trim().lowercase())
            put(COLUMN_USER_PASSWORD_HASH, user.passwordHash)
            put(COLUMN_USER_AGE, user.age)
            put(COLUMN_USER_WEIGHT, user.weight)
            put(COLUMN_USER_HEIGHT, user.height)
            put(COLUMN_USER_SEX, user.sex.lowercase())
            put(COLUMN_USER_DAILY_GOAL, user.dailyWaterGoal)
            put(COLUMN_USER_NOTIFICATION_FREQ, user.notificationFrequency)
            put(COLUMN_USER_SECURITY_QUESTION, user.securityQuestion)
            put(COLUMN_USER_SECURITY_ANSWER_HASH, user.securityAnswerHash)
            put(COLUMN_USER_UPDATED_AT, getCurrentDateTime())
        }
        return db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(user.id.toString()))
    }

    fun updateUserPassword(email: String, newPassword: String): Boolean {
        return try {
            val db = writableDatabase

            // Hash the new password before storing it
            val hashedPassword = hashPassword(newPassword)

            val values = ContentValues().apply {
                put(COLUMN_USER_PASSWORD_HASH, hashedPassword) // Use the correct column name
                put(COLUMN_USER_UPDATED_AT, getCurrentDateTime()) // Update the timestamp
            }

            val rowsAffected = db.update(
                TABLE_USERS, // Use the constant for table name
                values,
                "$COLUMN_USER_EMAIL = ?", // Use the constant for email column
                arrayOf(email.trim().lowercase()) // Normalize email for consistency
            )

            Log.d("DatabaseHelper", "Password update for $email: $rowsAffected rows affected")
            rowsAffected > 0
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error updating password for $email", e)
            false
        }
    }

    fun deleteUser(userId: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_USERS, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
    }

    // Water intake functions
    fun logWaterIntake(intake: WaterIntake): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INTAKE_USER_ID, intake.userId)
            put(COLUMN_INTAKE_AMOUNT, intake.amount)
            put(COLUMN_INTAKE_DATE, intake.date)
            put(COLUMN_INTAKE_TIME, intake.time)
            put(COLUMN_INTAKE_CREATED_AT, intake.createdAt)
        }
        return db.insert(TABLE_WATER_INTAKE, null, values)
    }

    fun getTodayWaterIntake(userId: Long): Int {
        val today = getCurrentDate()
        return getDailyWaterIntake(userId, today)
    }

    fun getDailyWaterIntake(userId: Long, date: String): Int {
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(
                """
                SELECT SUM($COLUMN_INTAKE_AMOUNT) as total 
                FROM $TABLE_WATER_INTAKE 
                WHERE $COLUMN_INTAKE_USER_ID = ? AND $COLUMN_INTAKE_DATE = ?
                """.trimIndent(),
                arrayOf(userId.toString(), date)
            )

            val total = if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                0
            }
            return total
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception in getDailyWaterIntake: ${e.message}", e)
            return 0
        } finally {
            cursor?.close()
        }
    }

    // Method for Drink Frequency in Analytics Water Report
    fun getIntakeRecordsBetweenDates(userId: Long, startDate: String, endDate: String): List<WaterIntake> {
        val intakeRecords = mutableListOf<WaterIntake>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        val query = """
            SELECT * FROM $TABLE_WATER_INTAKE 
            WHERE $COLUMN_INTAKE_USER_ID = ? 
            AND $COLUMN_INTAKE_DATE BETWEEN ? AND ?
            ORDER BY $COLUMN_INTAKE_DATE ASC, $COLUMN_INTAKE_TIME ASC
        """.trimIndent()

        try {
            cursor = db.rawQuery(query, arrayOf(userId.toString(), startDate, endDate))

            if (cursor.moveToFirst()) {
                do {
                    val intake = WaterIntake(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_ID)),
                        userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_USER_ID)),
                        amount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_AMOUNT)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_DATE)),
                        time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_TIME)),
                        createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_CREATED_AT))
                    )
                    intakeRecords.add(intake)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception in getIntakeRecordsBetweenDates: ${e.message}", e)
        } finally {
            cursor?.close()
        }

        return intakeRecords
    }

    fun getWaterIntakeHistory(userId: Long, date: String): List<WaterIntake> {
        val db = this.readableDatabase
        val intakeList = mutableListOf<WaterIntake>()
        var cursor: Cursor? = null

        try {
            cursor = db.query(
                TABLE_WATER_INTAKE,
                null,
                "$COLUMN_INTAKE_USER_ID = ? AND $COLUMN_INTAKE_DATE = ?",
                arrayOf(userId.toString(), date),
                null,
                null,
                "$COLUMN_INTAKE_TIME ASC"
            )

            while (cursor.moveToNext()) {
                intakeList.add(cursorToWaterIntake(cursor))
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception in getWaterIntakeHistory: ${e.message}", e)
        } finally {
            cursor?.close()
        }

        return intakeList
    }

    fun getLast30DaysSummary(userId: Long): List<DailyIntakeSummary> {
        val db = this.readableDatabase
        val summaryList = mutableListOf<DailyIntakeSummary>()
        var cursor: Cursor? = null

        // Get user's daily goal
        val user = getUserById(userId)
        val dailyGoal = user?.dailyWaterGoal ?: 2000

        try {
            cursor = db.rawQuery(
                """
                SELECT 
                    $COLUMN_INTAKE_DATE,
                    SUM($COLUMN_INTAKE_AMOUNT) as total_intake
                FROM $TABLE_WATER_INTAKE 
                WHERE $COLUMN_INTAKE_USER_ID = ? 
                    AND $COLUMN_INTAKE_DATE >= date('now', '-30 days')
                GROUP BY $COLUMN_INTAKE_DATE
                ORDER BY $COLUMN_INTAKE_DATE DESC
                """.trimIndent(),
                arrayOf(userId.toString())
            )

            while (cursor.moveToNext()) {
                val date = cursor.getString(0)
                val totalIntake = cursor.getInt(1)
                val goalAchieved = totalIntake >= dailyGoal

                summaryList.add(DailyIntakeSummary(date, totalIntake, goalAchieved))
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception in getLast30DaysSummary: ${e.message}", e)
        } finally {
            cursor?.close()
        }

        return summaryList
    }

    fun deleteWaterIntake(intakeId: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_WATER_INTAKE, "$COLUMN_INTAKE_ID = ?", arrayOf(intakeId.toString()))
    }

    // Utility functions
    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
            passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD_HASH)),
            age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_AGE)),
            weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_USER_WEIGHT)),
            height = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_USER_HEIGHT)),
            sex = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_SEX)),
            dailyWaterGoal = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_DAILY_GOAL)),
            notificationFrequency = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_NOTIFICATION_FREQ)),
            securityQuestion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_SECURITY_QUESTION)) ?: "",
            securityAnswerHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_SECURITY_ANSWER_HASH)) ?: "",
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_CREATED_AT)),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_UPDATED_AT)),
            googleId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_GOOGLE_ID)),
            facebookId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_FACEBOOK_ID))
        )
    }

    private fun cursorToWaterIntake(cursor: Cursor): WaterIntake {
        return WaterIntake(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_ID)),
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_USER_ID)),
            amount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_AMOUNT)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_DATE)),
            time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_TIME)),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INTAKE_CREATED_AT))
        )
    }

    // Helper function to calculate recommended daily water intake
    fun calculateRecommendedWaterIntake(weight: Double, sex: String): Int {
        // Basic calculation: 35ml per kg for men, 31ml per kg for women
        val multiplier = if (sex.lowercase() == "male") 35.0 else 31.0
        return (weight * multiplier).toInt()
    }

    // Function to check if daily goal is achieved
    fun isDailyGoalAchieved(userId: Long, date: String = getCurrentDate()): Boolean {
        val user = getUserById(userId) ?: return false
        val dailyIntake = getDailyWaterIntake(userId, date)
        return dailyIntake >= user.dailyWaterGoal
    }

    // ADD THIS METHOD HERE - INSIDE THE CLASS
    fun getConsecutiveGoalAchievementStreak(userId: Long): Int {
        val user = getUserById(userId) ?: return 0
        val dailyGoal = user.dailyWaterGoal

        val db = this.readableDatabase
        var cursor: Cursor? = null
        var streak = 0

        try {
            // Get daily totals for the last 365 days, ordered by date descending
            cursor = db.rawQuery(
                """
                SELECT 
                    $COLUMN_INTAKE_DATE,
                    SUM($COLUMN_INTAKE_AMOUNT) as daily_total
                FROM $TABLE_WATER_INTAKE 
                WHERE $COLUMN_INTAKE_USER_ID = ? 
                    AND $COLUMN_INTAKE_DATE >= date('now', '-365 days')
                GROUP BY $COLUMN_INTAKE_DATE
                ORDER BY $COLUMN_INTAKE_DATE DESC
                """.trimIndent(),
                arrayOf(userId.toString())
            )

            val today = getCurrentDate()

            if (cursor.moveToFirst()) {
                do {
                    val date = cursor.getString(0)
                    val dailyTotal = cursor.getInt(1)

                    if (dailyTotal >= dailyGoal) {
                        streak++
                    } else {
                        // If this is today and there's some intake, don't break streak yet
                        if (date == today && dailyTotal > 0) {
                            continue
                        } else {
                            // Past day didn't meet goal or today with no intake, break streak
                            break
                        }
                    }

                } while (cursor.moveToNext())
            }

        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception in getConsecutiveGoalAchievementStreak: ${e.message}", e)
            return 0
        } finally {
            cursor?.close()
        }

        return streak
    }

}
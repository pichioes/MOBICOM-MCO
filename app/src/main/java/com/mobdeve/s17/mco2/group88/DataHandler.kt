package com.mobdeve.s17.mco2.group88

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

// Updated User data class with password
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
    val createdAt: String = getCurrentDateTime(),
    val updatedAt: String = getCurrentDateTime()
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

// Password hashing utility
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
        private const val DATABASE_VERSION = 2 // Increment version for schema change

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
        private const val COLUMN_USER_CREATED_AT = "created_at"
        private const val COLUMN_USER_UPDATED_AT = "updated_at"

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
        // Create users table with password field
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
                $COLUMN_USER_CREATED_AT TEXT NOT NULL,
                $COLUMN_USER_UPDATED_AT TEXT NOT NULL
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

        db.execSQL(createUsersTable)
        db.execSQL(createWaterIntakeTable)

        // Create indexes for better performance
        db.execSQL("CREATE INDEX idx_water_intake_user_date ON $TABLE_WATER_INTAKE($COLUMN_INTAKE_USER_ID, $COLUMN_INTAKE_DATE)")
        db.execSQL("CREATE INDEX idx_users_email ON $TABLE_USERS($COLUMN_USER_EMAIL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add password column to existing users table
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_PASSWORD_HASH TEXT DEFAULT ''")
        }
    }

    // User management functions
    fun insertUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, user.name)
            put(COLUMN_USER_EMAIL, user.email)
            put(COLUMN_USER_PASSWORD_HASH, user.passwordHash)
            put(COLUMN_USER_AGE, user.age)
            put(COLUMN_USER_WEIGHT, user.weight)
            put(COLUMN_USER_HEIGHT, user.height)
            put(COLUMN_USER_SEX, user.sex)
            put(COLUMN_USER_DAILY_GOAL, user.dailyWaterGoal)
            put(COLUMN_USER_NOTIFICATION_FREQ, user.notificationFrequency)
            put(COLUMN_USER_CREATED_AT, user.createdAt)
            put(COLUMN_USER_UPDATED_AT, user.updatedAt)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUserByEmail(email: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val user = cursorToUser(cursor)
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    // New function for authentication
    fun authenticateUser(email: String, password: String): User? {
        val user = getUserByEmail(email)
        return if (user != null && verifyPassword(password, user.passwordHash)) {
            user
        } else {
            null
        }
    }

    fun getUserById(userId: Long): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val user = cursorToUser(cursor)
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun updateUser(user: User): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, user.name)
            put(COLUMN_USER_EMAIL, user.email)
            put(COLUMN_USER_PASSWORD_HASH, user.passwordHash)
            put(COLUMN_USER_AGE, user.age)
            put(COLUMN_USER_WEIGHT, user.weight)
            put(COLUMN_USER_HEIGHT, user.height)
            put(COLUMN_USER_SEX, user.sex)
            put(COLUMN_USER_DAILY_GOAL, user.dailyWaterGoal)
            put(COLUMN_USER_NOTIFICATION_FREQ, user.notificationFrequency)
            put(COLUMN_USER_UPDATED_AT, getCurrentDateTime())
        }
        return db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(user.id.toString()))
    }

    fun deleteUser(userId: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_USERS, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
    }

    // Water intake functions (unchanged)
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
        val cursor = db.rawQuery(
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
        cursor.close()
        return total
    }

    // Method for Drink Frequency in Analytics Water Report
    fun getIntakeRecordsBetweenDates(userId: Long, startDate: String, endDate: String): List<WaterIntake> {
        val intakeRecords = mutableListOf<WaterIntake>()
        val db = this.readableDatabase

        val query = """
        SELECT * FROM $TABLE_WATER_INTAKE 
        WHERE $COLUMN_INTAKE_USER_ID = ? 
        AND $COLUMN_INTAKE_DATE BETWEEN ? AND ?
        ORDER BY $COLUMN_INTAKE_DATE ASC, $COLUMN_INTAKE_TIME ASC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), startDate, endDate))

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

        cursor.close()
        return intakeRecords
    }

    fun getWaterIntakeHistory(userId: Long, date: String): List<WaterIntake> {
        val db = this.readableDatabase
        val intakeList = mutableListOf<WaterIntake>()

        val cursor = db.query(
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
        cursor.close()
        return intakeList
    }

    fun getLast30DaysSummary(userId: Long): List<DailyIntakeSummary> {
        val db = this.readableDatabase
        val summaryList = mutableListOf<DailyIntakeSummary>()

        // Get user's daily goal
        val user = getUserById(userId)
        val dailyGoal = user?.dailyWaterGoal ?: 2000

        val cursor = db.rawQuery(
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
        cursor.close()
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
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_CREATED_AT)),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_UPDATED_AT))
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
}
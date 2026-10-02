package com.gustavobento.mygym.data.repository

import com.gustavobento.mygym.data.local.LocalStorage
import com.gustavobento.mygym.model.Goal
import com.gustavobento.mygym.model.Sex
import com.gustavobento.mygym.model.User
import com.gustavobento.mygym.util.PasswordHasher
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Cadastro, login e sessão do usuário.
 *
 * As contas ficam salvas em JSON no LocalStorage. Para cada conta guardamos
 * os dados do usuário + salt + hash da senha (nunca a senha em si).
 */
class AuthRepository(private val storage: LocalStorage) {

    /** Conta completa (uso interno): o hash da senha não sai deste repositório. */
    private data class Account(val user: User, val salt: String, val passwordHash: String)

    fun isEmailRegistered(email: String): Boolean {
        val cleanEmail = normalizeEmail(email)
        return loadAccounts().any { it.user.email == cleanEmail }
    }

    /**
     * Cria uma nova conta. Retorna null se o e-mail já estiver cadastrado.
     * @param startSession se true, o usuário já fica logado.
     */
    fun register(
        name: String,
        email: String,
        password: String,
        sex: Sex,
        age: Int,
        weightKg: Double,
        heightCm: Double,
        goal: Goal,
        startSession: Boolean = true,
    ): User? {
        if (isEmailRegistered(email)) return null

        val user = User(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            email = normalizeEmail(email),
            sex = sex,
            age = age,
            weightKg = weightKg,
            heightCm = heightCm,
            goal = goal,
        )
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)

        val accounts = loadAccounts().toMutableList()
        accounts.add(Account(user, salt, hash))
        saveAccounts(accounts)

        if (startSession) storage.saveString(KEY_SESSION, user.id)
        return user
    }

    /** Retorna o usuário se e-mail e senha estiverem corretos; senão, null. */
    fun login(email: String, password: String): User? {
        val cleanEmail = normalizeEmail(email)
        val account = loadAccounts().find { it.user.email == cleanEmail } ?: return null

        if (!PasswordHasher.verify(password, account.salt, account.passwordHash)) return null

        storage.saveString(KEY_SESSION, account.user.id)
        return account.user
    }

    /** Usuário logado no momento, ou null se ninguém estiver logado. */
    fun currentUser(): User? {
        val userId = storage.readString(KEY_SESSION) ?: return null
        return loadAccounts().find { it.user.id == userId }?.user
    }

    fun logout() {
        storage.remove(KEY_SESSION)
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase()
    }

    // ---------- Conversão para JSON (salvar) e de JSON (ler) ----------

    private fun loadAccounts(): List<Account> {
        val json = storage.readString(KEY_ACCOUNTS) ?: return emptyList()
        val array = JSONArray(json)
        val accounts = mutableListOf<Account>()
        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val user = User(
                id = item.getString("id"),
                name = item.getString("name"),
                email = item.getString("email"),
                sex = Sex.valueOf(item.getString("sex")),
                age = item.getInt("age"),
                weightKg = item.getDouble("weightKg"),
                heightCm = item.getDouble("heightCm"),
                goal = Goal.valueOf(item.getString("goal")),
            )
            accounts.add(Account(user, item.getString("salt"), item.getString("passwordHash")))
        }
        return accounts
    }

    private fun saveAccounts(accounts: List<Account>) {
        val array = JSONArray()
        for (account in accounts) {
            val item = JSONObject()
            item.put("id", account.user.id)
            item.put("name", account.user.name)
            item.put("email", account.user.email)
            item.put("sex", account.user.sex.name)
            item.put("age", account.user.age)
            item.put("weightKg", account.user.weightKg)
            item.put("heightCm", account.user.heightCm)
            item.put("goal", account.user.goal.name)
            item.put("salt", account.salt)
            item.put("passwordHash", account.passwordHash)
            array.put(item)
        }
        storage.saveString(KEY_ACCOUNTS, array.toString())
    }

    companion object {
        private const val KEY_ACCOUNTS = "contas"
        private const val KEY_SESSION = "usuario_logado"
    }
}

const TOKEN_KEY = 'blog_token'
const USER_KEY = 'blog_user'

export const setToken = (token) => {
    localStorage.setItem(TOKEN_KEY, token)
}

export const getToken = () => {
    return localStorage.getItem(TOKEN_KEY)
}

export const removeToken = () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
}

export const setUser = (user) => {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export const getUser = () => {
    const user = localStorage.getItem(USER_KEY)
    if (!user) return null
    try {
        return JSON.parse(user)
    } catch {
        removeToken()
        return null
    }
}

export const isLoggedIn = () => {
    return !!getToken()
}

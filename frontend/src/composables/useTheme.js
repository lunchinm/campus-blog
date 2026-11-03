import { ref } from 'vue'

const THEME_KEY = 'blog_theme'

const isDark = ref(localStorage.getItem(THEME_KEY) === 'dark')

export function useTheme() {
  const toggleTheme = () => {
    isDark.value = !isDark.value
    localStorage.setItem(THEME_KEY, isDark.value ? 'dark' : 'light')
    applyTheme()
  }

  const applyTheme = () => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  applyTheme()

  return {
    isDark,
    toggleTheme
  }
}

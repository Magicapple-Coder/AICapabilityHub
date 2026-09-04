const asBoolean = (value: string | undefined, fallback: boolean): boolean => {
  if (value === undefined) return fallback
  return ['true', '1', 'yes', 'on'].includes(value.toLowerCase())
}

export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  useMockLogin: asBoolean(import.meta.env.VITE_USE_MOCK_LOGIN, true),
  devToken: import.meta.env.VITE_DEV_TOKEN || '',
}

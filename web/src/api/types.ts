export interface Result<T> {
  code: number
  message: string
  data: T | null
  requestId: string
}

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginData {
  token: string
  user_id?: string
  user_name?: string
  expires_in?: number
}

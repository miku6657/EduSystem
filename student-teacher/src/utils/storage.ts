import type {
  DbId,
} from '@/types/user'

const TOKEN_KEY =
  'st_token'

const ROLE_KEY =
  'st_role'

const NAME_KEY =
  'st_name'

const USER_ID_KEY =
  'st_user_id'

const USERNAME_KEY =
  'st_username'

const BUSINESS_ID_KEY =
  'st_business_id'

const PROFILE_KEY =
  'st_profile'

export function getToken():
  string {
  return sessionStorage.getItem(
    TOKEN_KEY,
  ) ?? ''
}

export function setToken(
  token: string,
): void {
  sessionStorage.setItem(
    TOKEN_KEY,
    token,
  )
}

export function getRole():
  string {
  return localStorage.getItem(
    ROLE_KEY,
  ) ?? ''
}

export function setRole(
  role: string,
): void {
  localStorage.setItem(
    ROLE_KEY,
    role,
  )
}

export function getName():
  string {
  return localStorage.getItem(
    NAME_KEY,
  ) ?? ''
}

export function setName(
  name: string,
): void {
  localStorage.setItem(
    NAME_KEY,
    name,
  )
}

export function getUsername():
  string {
  return localStorage.getItem(
    USERNAME_KEY,
  ) ?? ''
}

export function setUsername(
  username: string,
): void {
  localStorage.setItem(
    USERNAME_KEY,
    username,
  )
}

export function getUserId():
  DbId {
  return localStorage.getItem(
    USER_ID_KEY,
  ) ?? ''
}

export function setUserId(
  id: DbId,
): void {
  localStorage.setItem(
    USER_ID_KEY,
    id,
  )
}

export function getBusinessId():
  DbId {
  return localStorage.getItem(
    BUSINESS_ID_KEY,
  ) ?? ''
}

export function setBusinessId(
  id: DbId,
): void {
  localStorage.setItem(
    BUSINESS_ID_KEY,
    id,
  )
}

export function getCachedProfile<T>():
  T | null {

  const raw =
    localStorage.getItem(
      PROFILE_KEY,
    )

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(
      raw,
    ) as T
  } catch {
    return null
  }
}

export function setCachedProfile(
  profile: unknown,
): void {
  localStorage.setItem(
    PROFILE_KEY,
    JSON.stringify(
      profile,
    ),
  )
}

/**
 * 清除师生端全部登录数据。
 */
export function clearAuth():
  void {

  sessionStorage.removeItem(
    TOKEN_KEY,
  )

  localStorage.removeItem(
    ROLE_KEY,
  )

  localStorage.removeItem(
    NAME_KEY,
  )

  localStorage.removeItem(
    USER_ID_KEY,
  )

  localStorage.removeItem(
    USERNAME_KEY,
  )

  localStorage.removeItem(
    BUSINESS_ID_KEY,
  )

  localStorage.removeItem(
    PROFILE_KEY,
  )
}

import { http } from '@/utils/request'

export interface Term {
  id: string
  name: string
  startDate: string
  endDate: string
  status?: number
}

export function getTermList() {
  return http.get<Term[]>('/terms')
}

export async function getCurrentTerm() {
  const terms = await getTermList()

  if (!terms || terms.length === 0) {
    return null
  }

  const activeTerm = terms.find(
    (term) => Number(term.status) === 1,
  )

  if (activeTerm) {
    return activeTerm
  }

  const today = new Date()

  const dateTerm = terms.find((term) => {
    const start = new Date(`${term.startDate}T00:00:00`)
    const end = new Date(`${term.endDate}T23:59:59`)

    return today >= start && today <= end
  })

  if (dateTerm) {
    return dateTerm
  }

  return [...terms].sort(
    (a, b) =>
      new Date(b.startDate).getTime()
      - new Date(a.startDate).getTime(),
  )[0]
}

export function addTerm(data: Term) {
  return http.post('/terms', data)
}

export function updateTerm(data: Term) {
  return http.put(`/terms/${data.id}`, data)
}

export function deleteTerm(id: string) {
  return http.delete(`/terms/${id}`)
}
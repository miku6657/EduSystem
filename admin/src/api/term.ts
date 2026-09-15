import { http } from '@/utils/request'


export interface Term {

  id:number

  name:string

  startDate:string

  endDate:string

  status?:number

}


/**
 * 查询全部学期
 */
export function getTermList(){

  return http.get<Term[]>(
    '/terms'
  )

}


/**
 * 当前学期
 */
export function getCurrentTerm(){

  return getTermList().then((terms) => terms.find((term) => term.status === 1) ?? terms[0])

}


/**
 * 新增学期
 */
export function addTerm(
    data:Term
){

  return http.post(
    '/terms',
    data
  )

}


/**
 * 修改学期
 */
export function updateTerm(
    data:Term
){

  return http.put(
    `/terms/${data.id}`,
    data
  )

}


/**
 * 删除学期
 */
export function deleteTerm(
    id:number
){

  return http.delete(
    `/terms/${id}`
  )

}
# EduSystem 后端修改意见

> 目标：以当前 GitHub 后端代码为准，修复 admin 前端联调中的接口、字段和数据一致性问题。

## 一、必须修改：教室审批状态筛选

当前 `ClassroomApplyController` 大致实现如下：

```java
@GetMapping
public Result<List<ClassroomApply>> list(
        @RequestParam(required = false) String status
) {
    return Result.success(classroomApplyService.list());
}
```

虽然接收了 `status` 参数，但没有使用，因此前端选择“待审核”“已通过”“已驳回”时，后端始终返回全部数据。

建议改为：

```java
@GetMapping
public Result<List<ClassroomApply>> list(
        @RequestParam(required = false) String status
) {
    if (status == null || status.isBlank()) {
        return Result.success(classroomApplyService.list());
    }

    return Result.success(
        classroomApplyService.lambdaQuery()
            .eq(ClassroomApply::getStatus, status)
            .orderByDesc(ClassroomApply::getCreateTime)
            .list()
    );
}
```

需要导入：

```java
import com.keshe.edumanage.entity.base.ClassroomApply;
```

前端请求：

```text
GET /api/classroom-applies?status=待审核
GET /api/classroom-applies?status=已通过
GET /api/classroom-applies?status=已驳回
GET /api/classroom-applies
```

## 二、建议修改：教室申请返回 VO

后端实体字段：

```java
private LocalDate applyDate;
private LocalDateTime createTime;
private Long roomId;
```

前端使用字段：

```text
date
applyTime
roomName
```

建议不要直接返回实体，新增返回对象：

```java
@Data
public class ClassroomApplyVO {
    private Long id;
    private Long roomId;
    private String roomName;
    private String applicant;
    private String className;
    private LocalDate date;
    private String timeSlot;
    private String purpose;
    private String reason;
    private String status;
    private LocalDateTime applyTime;
}
```

映射关系：

```java
vo.setDate(apply.getApplyDate());
vo.setApplyTime(apply.getCreateTime());
```

`roomName` 根据 `roomId` 查询 `Classroom` 后补充。

这样前端可以直接使用：

```text
roomName
 date
applyTime
```

## 三、课程增加教师 ID 和学期 ID

当前 `Course` 实体只有：

```java
private String courseCode;
private String name;
private Double credit;
private String type;
private Long teachingGroupId;
```

当前没有：

```java
private Long teacherId;
private Long termId;
```

如果课程需要保存授课教师和学期，需要修改数据库：

```sql
ALTER TABLE base_course
ADD COLUMN teacher_id BIGINT NULL;

ALTER TABLE base_course
ADD COLUMN term_id BIGINT NULL;
```

修改实体：

```java
private Long teacherId;
private Long termId;
```

建议增加外键：

```sql
ALTER TABLE base_course
ADD CONSTRAINT fk_course_teacher
FOREIGN KEY (teacher_id) REFERENCES base_teacher(id);

ALTER TABLE base_course
ADD CONSTRAINT fk_course_term
FOREIGN KEY (term_id) REFERENCES base_term(id);
```

如果暂时不需要教师 ID 和学期 ID，前端不能提交这两个字段，否则后端会忽略。

## 四、课程教研室外键校验

数据库错误：

```text
Cannot add or update a child row: a foreign key constraint fails
(fk_course_group)
```

原因是课程提交的 `teaching_group_id` 在 `base_teaching_group.id` 中不存在。

建议在课程保存和修改前校验：

```java
if (course.getTeachingGroupId() != null
        && teachingGroupService.getById(course.getTeachingGroupId()) == null) {
    throw new BusinessException("教研室不存在");
}
```

同时确认：

```text
base_course.teaching_group_id
```

必须对应：

```text
base_teaching_group.id
```

前端应使用教研室下拉框，不应让用户手动输入教研室 ID。

## 五、学期当前接口

当前后端 `TermService` 已有：

```java
Term getCurrentTerm();
```

但 `TermController` 没有暴露当前学期接口。

建议新增：

```java
@GetMapping("/current")
public Result<Term> current() {
    return Result.success(termService.getCurrentTerm());
}
```

接口：

```text
GET /api/terms/current
```

当前学期建议按日期判断：

```text
startDate <= 当前日期 <= endDate
```

而不是单纯依赖 `status` 字段。

## 六、仪表盘统计接口

当前后端没有：

```text
GET /api/dashboard/statistics
```

因此请求会出现：

```text
NoResourceFoundException: No static resource api/dashboard/statistics
```

有两种方案。

### 方案 A：不新增后端接口

前端分别调用已有接口：

```text
/api/courses
/api/classrooms
/api/graduate-checks
/api/classroom-applies
```

然后在前端组合统计数据。

### 方案 B：新增统一仪表盘接口

新增 Controller：

```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/statistics")
    public Result<DashboardStatisticsVO> statistics() {
        return Result.success(dashboardService.statistics());
    }
}
```

返回对象：

```java
@Data
public class DashboardStatisticsVO {
    private Long courseCount;
    private Long classroomCount;
    private Long todayAdjustCount;
    private Long pendingGraduationCount;
    private List<PendingApprovalVO> pendingApprovals;
}
```

## 七、已确认的后端接口路径

### 课程

```text
GET    /api/courses
GET    /api/courses/{id}
POST   /api/courses
PUT    /api/courses/{id}
DELETE /api/courses/{id}
```

查询参数：

```text
pageNo
pageSize
keyword
type
teachingGroupId
courseCode
```

### 教室

```text
GET    /api/classrooms
GET    /api/classrooms/{id}
GET    /api/classrooms/free
POST   /api/classrooms
PUT    /api/classrooms/{id}
DELETE /api/classrooms/{id}
```

### 教室申请

```text
POST /api/classroom-applies
GET  /api/classroom-applies/my
GET  /api/classroom-applies
PUT  /api/classroom-applies/{id}/approve
PUT  /api/classroom-applies/{id}/reject
```

### 学期

```text
GET    /api/terms
GET    /api/terms/{id}
POST   /api/terms
PUT    /api/terms/{id}
DELETE /api/terms/{id}
```

### 系部

```text
GET    /api/departments
GET    /api/departments/{id}
POST   /api/departments
PUT    /api/departments/{id}
DELETE /api/departments/{id}
```

### 专业

```text
GET    /api/majors
GET    /api/majors/{id}
GET    /api/majors?departmentId={id}
POST   /api/majors
PUT    /api/majors/{id}
DELETE /api/majors/{id}
```

### 教研室

```text
GET    /api/teaching-groups
GET    /api/teaching-groups/{id}
GET    /api/teaching-groups?departmentId={id}
POST   /api/teaching-groups
PUT    /api/teaching-groups/{id}
DELETE /api/teaching-groups/{id}
```

## 八、修改优先级

1. 修复 `ClassroomApplyController` 的 `status` 筛选。
2. 增加教室申请 VO，统一 `applyDate/createTime` 与前端字段。
3. 确认课程是否需要增加 `teacher_id`、`term_id`。
4. 增加课程教研室外键校验。
5. 给 `TermController` 增加 `/current`。
6. 根据需要新增 `/dashboard/statistics`。

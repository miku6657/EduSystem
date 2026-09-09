-- ============================================================
-- 教学过程管理系统 建表脚本
-- 数据库：eduSYSTEM（MySQL 8.x，InnoDB，utf8mb4）
-- 字段与实体类 com.keshe.edumanage.entity.** 一一对应
-- （MyBatis-Plus 自动完成 驼峰 ↔ 下划线 映射）
-- 公共字段：id / create_time / update_time 来自 BaseEntity
-- ============================================================

CREATE DATABASE IF NOT EXISTS eduSYSTEM DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE eduSYSTEM;

-- ============ 一、系统基础表 ============

-- 1. 系统用户表（用户登录认证）
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username    VARCHAR(50)  NOT NULL                COMMENT '用户名',
    password    VARCHAR(100) NOT NULL                COMMENT '密码',
    role        VARCHAR(20)  DEFAULT NULL            COMMENT '角色',
    status      INT          DEFAULT 1               COMMENT '状态',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 系统角色表（管理员、教师、学生角色定义）
CREATE TABLE sys_role (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_name   VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50) NOT NULL COMMENT '角色编码',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- ============ 二、教学基础数据表 ============

-- 1. 校区信息表
CREATE TABLE base_campus (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(100) NOT NULL COMMENT '校区名称',
    address     VARCHAR(200) DEFAULT NULL COMMENT '地址',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区信息表';

-- 2. 系部信息表
CREATE TABLE base_department (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(100) NOT NULL COMMENT '系部名称',
    code        VARCHAR(50)  DEFAULT NULL COMMENT '系部编码',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系部信息表';

-- 3. 教研室信息表
CREATE TABLE base_teaching_group (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name          VARCHAR(100) NOT NULL COMMENT '教研室名称',
    department_id BIGINT       DEFAULT NULL COMMENT '所属系部ID',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教研室信息表';

-- 4. 教师信息表（专职/校内兼职/校外兼职分类查询）
CREATE TABLE base_teacher (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    teacher_no        VARCHAR(50)  NOT NULL COMMENT '教师工号',
    name              VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender            VARCHAR(10)  DEFAULT NULL COMMENT '性别',
    type              VARCHAR(20)  DEFAULT NULL COMMENT '教师类型',
    phone             VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    department_id     BIGINT       DEFAULT NULL COMMENT '系部ID',
    teaching_group_id BIGINT       DEFAULT NULL COMMENT '教研室ID',
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_no (teacher_no),
    KEY idx_department_id (department_id),
    KEY idx_teaching_group_id (teaching_group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

-- 5. 专业信息表
CREATE TABLE base_major (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name          VARCHAR(100) NOT NULL COMMENT '专业名称',
    code          VARCHAR(50)  NOT NULL COMMENT '专业代码',
    department_id BIGINT       DEFAULT NULL COMMENT '系部ID',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业信息表';

-- 6. 班级信息表
CREATE TABLE base_class (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name          VARCHAR(100) NOT NULL COMMENT '班级名称',
    major_id      BIGINT       DEFAULT NULL COMMENT '专业ID',
    campus_id     BIGINT       DEFAULT NULL COMMENT '校区ID',
    grade         VARCHAR(10)  DEFAULT NULL COMMENT '年级',
    student_count INT          DEFAULT 0 COMMENT '人数',
    counselor     VARCHAR(50)  DEFAULT NULL COMMENT '辅导员',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_major_id (major_id),
    KEY idx_campus_id (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

-- 7. 学生信息表
CREATE TABLE base_student (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    student_no  VARCHAR(50)  NOT NULL COMMENT '学号',
    name        VARCHAR(50)  NOT NULL COMMENT '姓名',
    gender      VARCHAR(10)  DEFAULT NULL COMMENT '性别',
    class_id    BIGINT       DEFAULT NULL COMMENT '班级ID',
    phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    status      VARCHAR(20)  DEFAULT NULL COMMENT '学籍状态',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 8. 课程信息表
CREATE TABLE base_course (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    course_code       VARCHAR(50)  NOT NULL COMMENT '课程代码',
    name              VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit            DOUBLE       DEFAULT NULL COMMENT '学分',
    type              VARCHAR(20)  DEFAULT NULL COMMENT '课程类型',
    teaching_group_id BIGINT       DEFAULT NULL COMMENT '所属教研室ID',
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_code (course_code),
    KEY idx_teaching_group_id (teaching_group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程信息表';

-- 9. 教室信息表（占用/空闲使用状况）
CREATE TABLE base_classroom (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    room_no     VARCHAR(50)  NOT NULL COMMENT '教室编号',
    campus_id   BIGINT       DEFAULT NULL COMMENT '所属校区ID',
    type        VARCHAR(20)  DEFAULT NULL COMMENT '类型：普通教室/机房/多媒体/实验室',
    area        DOUBLE       DEFAULT NULL COMMENT '面积（平方米）',
    capacity    INT          DEFAULT NULL COMMENT '座位数',
    status      VARCHAR(20)  DEFAULT NULL COMMENT '使用状况：占用/空闲',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_campus_id (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室信息表';

-- 10. 学期信息表
CREATE TABLE base_term (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(50)  NOT NULL COMMENT '学期名称',
    start_date  DATE         DEFAULT NULL COMMENT '开始日期',
    end_date    DATE         DEFAULT NULL COMMENT '结束日期',
    status      INT          DEFAULT NULL COMMENT '状态',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期信息表';

-- ============ 三、考务管理表 ============

-- 1. 考试信息表（期末考试安排）
CREATE TABLE exam_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(100) NOT NULL COMMENT '考试名称',
    course_id   BIGINT       DEFAULT NULL COMMENT '课程ID',
    term_id     BIGINT       DEFAULT NULL COMMENT '学期ID',
    exam_type   VARCHAR(20)  DEFAULT NULL COMMENT '考试类型',
    exam_date   DATE         DEFAULT NULL COMMENT '考试日期',
    start_time  TIME         DEFAULT NULL COMMENT '开始时间',
    end_time    TIME         DEFAULT NULL COMMENT '结束时间',
    status      VARCHAR(20)  DEFAULT NULL COMMENT '状态',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_term_id (term_id),
    KEY idx_exam_date (exam_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试信息表';

-- 2. 考试申请表（考核方式申报：教师申请，教研室/系主任/教务处审核）
CREATE TABLE exam_apply (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    course_id   BIGINT       NOT NULL COMMENT '课程ID',
    teacher_id  BIGINT       NOT NULL COMMENT '申报教师ID',
    apply_type  VARCHAR(20)  DEFAULT NULL COMMENT '考核方式',
    reason      VARCHAR(500) DEFAULT NULL COMMENT '申报理由',
    status      VARCHAR(20)  DEFAULT 'WAIT' COMMENT '状态：WAIT/PASS/FAIL',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试申请表';

-- 3. 考场安排表（考试与教室关联）
CREATE TABLE exam_room (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    exam_id      BIGINT   NOT NULL COMMENT '考试ID',
    classroom_id BIGINT   NOT NULL COMMENT '教室ID',
    seat_count   INT      DEFAULT NULL COMMENT '考场座位数',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_exam_id (exam_id),
    KEY idx_classroom_id (classroom_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考场安排表';

-- 4. 监考安排表（主监考/副监考）
CREATE TABLE exam_monitor (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    exam_id      BIGINT      NOT NULL COMMENT '考试ID',
    teacher_id   BIGINT      NOT NULL COMMENT '监考教师ID',
    monitor_role VARCHAR(20) DEFAULT NULL COMMENT '监考角色：主监考/副监考',
    create_time  DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_exam_id (exam_id),
    KEY idx_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监考安排表';

-- 5. 考试成绩表
CREATE TABLE exam_score (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    exam_id     BIGINT      NOT NULL COMMENT '考试ID',
    student_id  BIGINT      NOT NULL COMMENT '学生ID',
    score       DOUBLE      DEFAULT NULL COMMENT '成绩',
    status      VARCHAR(20) DEFAULT NULL COMMENT '成绩状态（缺考等）',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_exam_student (exam_id, student_id),
    KEY idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试成绩表';

-- 6. 补考重修表（补考安排 + 重修申请）
CREATE TABLE exam_retake (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    student_id  BIGINT      NOT NULL COMMENT '学生ID',
    course_id   BIGINT      NOT NULL COMMENT '课程ID',
    exam_id     BIGINT      DEFAULT NULL COMMENT '安排的考试ID（未安排时为空）',
    type        VARCHAR(20) DEFAULT NULL COMMENT '类型：补考/重修',
    create_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_student_id (student_id),
    KEY idx_course_id (course_id),
    KEY idx_exam_id (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补考重修表';

-- ============ 四、毕业管理表 ============

-- 1. 毕业资格审核表（学分审核 + 课程审核）
CREATE TABLE graduate_check (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    student_id    BIGINT       NOT NULL COMMENT '学生ID',
    check_status  VARCHAR(20)  DEFAULT 'WAIT' COMMENT '审核结论：WAIT/PASS/FAIL',
    credit_status VARCHAR(20)  DEFAULT NULL COMMENT '学分审核：PASS/FAIL',
    course_status VARCHAR(20)  DEFAULT NULL COMMENT '课程审核：PASS/FAIL',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    checker       VARCHAR(50)  DEFAULT NULL COMMENT '审核人',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业资格审核表';

-- 2. 毕业生信息表（毕业证编号、上报库）
CREATE TABLE graduate_student (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    student_id      BIGINT       NOT NULL COMMENT '学生ID',
    graduate_year   VARCHAR(10)  DEFAULT NULL COMMENT '毕业年份',
    certificate_no  VARCHAR(50)  DEFAULT NULL COMMENT '毕业证编号',
    graduate_status VARCHAR(20)  DEFAULT NULL COMMENT '毕业状态',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_certificate_no (certificate_no),
    KEY idx_student_id (student_id),
    KEY idx_graduate_year (graduate_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毕业生信息表';

-- 3. 专升本报名表
CREATE TABLE upgrade_apply (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    student_id   BIGINT       NOT NULL COMMENT '学生ID',
    school_name  VARCHAR(100) DEFAULT NULL COMMENT '报考院校',
    major_name   VARCHAR(100) DEFAULT NULL COMMENT '报考专业',
    apply_status VARCHAR(20)  DEFAULT 'WAIT' COMMENT '报名状态：WAIT/PASS/FAIL',
    remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_student_id (student_id),
    KEY idx_apply_status (apply_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专升本报名表';

-- ============ 五、考勤管理表 ============

-- 1. 教师考勤表（签到 + 指纹考勤数据导入）
CREATE TABLE teacher_attendance (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    teacher_id      BIGINT      NOT NULL COMMENT '教师ID',
    attendance_date DATE        NOT NULL COMMENT '考勤日期',
    status          VARCHAR(20) DEFAULT NULL COMMENT '考勤状态',
    check_time      DATETIME    DEFAULT NULL COMMENT '签到时间',
    create_time     DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_date (teacher_id, attendance_date),
    KEY idx_attendance_date (attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师考勤表';

-- 2. 教学日志表（授课内容及学生出勤情况，最迟每周末提交）
CREATE TABLE teaching_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    teacher_id    BIGINT       NOT NULL COMMENT '教师ID',
    course_id     BIGINT       DEFAULT NULL COMMENT '课程ID',
    class_id      BIGINT       DEFAULT NULL COMMENT '班级ID',
    teaching_date DATE         NOT NULL COMMENT '授课日期',
    content       TEXT         DEFAULT NULL COMMENT '授课内容',
    homework      VARCHAR(500) DEFAULT NULL COMMENT '作业布置',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_class_id (class_id),
    KEY idx_teaching_date (teaching_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学日志表';

-- 3. 学生考勤表（课程出勤记录）
CREATE TABLE student_attendance (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    student_id      BIGINT      NOT NULL COMMENT '学生ID',
    course_id       BIGINT      NOT NULL COMMENT '课程ID',
    attendance_date DATE        NOT NULL COMMENT '考勤日期',
    status          VARCHAR(20) DEFAULT NULL COMMENT '考勤状态',
    create_time     DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_course_date (student_id, course_id, attendance_date),
    KEY idx_course_id (course_id),
    KEY idx_attendance_date (attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生考勤表';

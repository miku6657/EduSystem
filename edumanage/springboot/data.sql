-- ============================================================
-- 教学过程管理系统 测试数据
-- 目标库：eduSYSTEM（先执行 schema.sql 建库建表，再执行本脚本）
-- 登录账号（明文密码）：admin/123456、teacher01/123456、
--                       teacher02/123456、student01/123456
-- ============================================================
USE eduSYSTEM;

-- ============ 一、系统基础数据 ============

INSERT INTO sys_user (id, username, password, role, status) VALUES
(1, 'admin',     '123456', 'ADMIN',   1),
(2, 'teacher01', '123456', 'TEACHER', 1),
(3, 'teacher02', '123456', 'TEACHER', 1),
(4, 'student01', '123456', 'STUDENT', 1);

INSERT INTO sys_role (id, role_name, role_code) VALUES
(1, '管理员', 'ADMIN'),
(2, '教师',   'TEACHER'),
(3, '学生',   'STUDENT');

-- ============ 二、教学基础数据 ============

-- 校区
INSERT INTO base_campus (id, name, address) VALUES
(1, '东校区', '武汉市洪山区珞瑜路 100 号'),
(2, '西校区', '武汉市江夏区藏龙岛大道 88 号');

-- 系部
INSERT INTO base_department (id, name, code) VALUES
(1, '信息工程系', 'XXGC'),
(2, '经济管理系', 'JJGL');

-- 教研室
INSERT INTO base_teaching_group (id, name, department_id) VALUES
(1, '软件技术教研室',   1),
(2, '计算机应用教研室', 1),
(3, '会计教研室',       2);

-- 教师（type：专职/校内兼职/校外兼职，支持分类查询）
INSERT INTO base_teacher (id, teacher_no, name, gender, type, phone, department_id, teaching_group_id) VALUES
(1, 'T001', '张伟', '男', '专职',     '13800000001', 1, 1),
(2, 'T002', '李娜', '女', '专职',     '13800000002', 1, 1),
(3, 'T003', '王强', '男', '校内兼职', '13800000003', 1, 2),
(4, 'T004', '赵敏', '女', '专职',     '13800000004', 2, 3);

-- 专业
INSERT INTO base_major (id, name, code, department_id) VALUES
(1, '软件技术',       'RJ01', 1),
(2, '计算机应用技术', 'JSJ01', 1),
(3, '大数据与会计',   'KJ01', 2);

-- 班级
INSERT INTO base_class (id, name, major_id, campus_id, grade, student_count, counselor) VALUES
(1, '软件技术2301班',     1, 1, '2023', 40, '刘老师'),
(2, '软件技术2302班',     1, 1, '2023', 38, '刘老师'),
(3, '大数据与会计2301班', 3, 2, '2023', 42, '陈老师');

-- 学生
INSERT INTO base_student (id, student_no, name, gender, class_id, phone, status) VALUES
(1, '2023005001', '王小明', '男', 1, '13900000001', '在读'),
(2, '2023005002', '陈红',   '女', 1, '13900000002', '在读'),
(3, '2023005003', '刘洋',   '男', 1, '13900000003', '在读'),
(4, '2023005004', '张雪',   '女', 1, '13900000004', '在读'),
(5, '2023005005', '孙磊',   '男', 2, '13900000005', '在读'),
(6, '2023005006', '周婷',   '女', 2, '13900000006', '在读'),
(7, '2023005007', '吴刚',   '男', 3, '13900000007', '在读'),
(8, '2023005008', '郑爽',   '女', 3, '13900000008', '在读');

-- 课程（credit 学分，type 考试/考查）
INSERT INTO base_course (id, course_code, name, credit, type, teaching_group_id) VALUES
(1, 'C001', 'Java程序设计', 4.0, '考试', 1),
(2, 'C002', '数据库原理',     3.5, '考试', 1),
(3, 'C003', 'Web前端开发',   3.0, '考查', 2),
(4, 'C004', '会计基础',       3.0, '考试', 3);

-- 教室（status：占用/空闲）
INSERT INTO base_classroom (id, room_no, campus_id, type, area, capacity, status) VALUES
(1, '教学楼A101',     1, '普通教室', 80,  60,  '空闲'),
(2, '教学楼A102',     1, '普通教室', 80,  60,  '空闲'),
(3, '实训楼B201机房', 1, '机房',     120, 50,  '空闲'),
(4, '图书馆报告厅',   1, '多媒体',   300, 200, '空闲'),
(5, '教学楼C301',     2, '普通教室', 80,  55,  '空闲');

-- 学期（status：1 进行中 / 0 已结束，当前学期覆盖系统日期）
INSERT INTO base_term (id, name, start_date, end_date, status) VALUES
(1, '2025-2026学年第二学期', '2026-03-01', '2026-07-10', 0),
(2, '2026-2027学年第一学期', '2026-09-01', '2027-01-20', 1);

-- ============ 三、考务数据 ============

-- 考核方式申报（status：WAIT 待审核 / PASS 通过 / FAIL 驳回）
INSERT INTO exam_apply (id, course_id, teacher_id, apply_type, reason, status) VALUES
(1, 1, 1, '闭卷', 'Java程序设计为专业核心课程，采用闭卷笔试考核', 'PASS'),
(2, 2, 2, '开卷', '数据库原理侧重设计与分析能力，采用开卷考核',   'WAIT'),
(3, 3, 3, '机考', 'Web前端开发采用上机实操考核',                   'PASS');

-- 考试安排（当前学期期末）
INSERT INTO exam_info (id, name, course_id, term_id, exam_type, exam_date, start_time, end_time, status) VALUES
(1, 'Java程序设计期末考试', 1, 2, '期末考试', '2027-01-10', '09:00', '11:00', 'ARRANGED'),
(2, '数据库原理期末考试',   2, 2, '期末考试', '2027-01-11', '09:00', '11:00', 'ARRANGED'),
(3, '会计基础期末考试',     4, 2, '期末考试', '2027-01-12', '14:00', '16:00', 'ARRANGED');

-- 考场安排（seat_count 取教室容量）
INSERT INTO exam_room (id, exam_id, classroom_id, seat_count) VALUES
(1, 1, 1, 60),
(2, 1, 2, 60),
(3, 2, 3, 50),
(4, 3, 5, 55);

-- 监考安排（MAIN 主监考 / SUB 副监考）
INSERT INTO exam_monitor (id, exam_id, teacher_id, monitor_role) VALUES
(1, 1, 1, 'MAIN'),
(2, 1, 2, 'SUB'),
(3, 2, 2, 'MAIN'),
(4, 2, 3, 'SUB'),
(5, 3, 4, 'MAIN');

-- 考试成绩（status：NORMAL 正常 / ABSENT 缺考）
INSERT INTO exam_score (id, exam_id, student_id, score, status) VALUES
(1,  1, 1, 85,   'NORMAL'),
(2,  1, 2, 92,   'NORMAL'),
(3,  1, 3, 45,   'NORMAL'),
(4,  1, 4, 78,   'NORMAL'),
(5,  2, 1, 70,   'NORMAL'),
(6,  2, 2, 88,   'NORMAL'),
(7,  2, 3, 55,   'NORMAL'),
(8,  2, 4, NULL, 'ABSENT');

-- 补考重修（type：补考/重修；exam_id 为空表示尚未安排场次）
INSERT INTO exam_retake (id, student_id, course_id, exam_id, type) VALUES
(1, 3, 2, NULL, '重修'),
(2, 4, 1, NULL, '补考');

-- ============ 四、毕业管理数据 ============

-- 毕业资格审核（check_status：WAIT/PASS/FAIL）
INSERT INTO graduate_check (id, student_id, check_status, credit_status, course_status, remark, checker) VALUES
(1, 1, 'PASS', 'PASS', 'PASS', '学分与课程全部合格，准予毕业', '教务处'),
(2, 2, 'FAIL', 'PASS', 'FAIL', '有 2 门课程不合格，需参加补考', '教务处'),
(3, 3, 'WAIT', NULL,   NULL,   NULL,                           NULL);

-- 毕业生（certificate_no 留空，可演示批量生成毕业证编号）
INSERT INTO graduate_student (id, student_id, graduate_year, certificate_no, graduate_status) VALUES
(1, 1, '2026', NULL, '已毕业'),
(2, 2, '2026', NULL, '在读');

-- 专升本报名（apply_status：WAIT/PASS/FAIL）
INSERT INTO upgrade_apply (id, student_id, school_name, major_name, apply_status, remark) VALUES
(1, 2, '华中科技大学', '计算机科学与技术', 'WAIT', NULL),
(2, 3, '武汉理工大学', '软件工程',         'PASS', '符合报名条件');

-- ============ 五、考勤数据 ============

-- 教师考勤（attendance_date 用当前日期，可演示签到/统计）
INSERT INTO teacher_attendance (id, teacher_id, attendance_date, status, check_time) VALUES
(1, 1, CURDATE(), '正常', NOW()),
(2, 2, CURDATE(), '正常', NOW()),
(3, 3, CURDATE() - INTERVAL 1 DAY, '迟到', NOW() - INTERVAL 1 DAY);

-- 教学日志（授课日期为昨天，演示周日志查询）
INSERT INTO teaching_log (id, teacher_id, course_id, class_id, teaching_date, content, homework) VALUES
(1, 1, 1, 1, CURDATE() - INTERVAL 1 DAY,
 '第三章 面向对象基础：类与对象、封装、构造方法', '完成课后编程题 3.5、3.6'),
(2, 2, 2, 1, CURDATE() - INTERVAL 1 DAY,
 '第四章 SQL 多表连接查询：内连接、外连接', '完成实验二：多表查询练习');

-- 学生考勤（软件技术2301班，昨天 Java 课）
INSERT INTO student_attendance (id, student_id, course_id, attendance_date, status) VALUES
(1, 1, 1, CURDATE() - INTERVAL 1 DAY, '正常'),
(2, 2, 1, CURDATE() - INTERVAL 1 DAY, '正常'),
(3, 3, 1, CURDATE() - INTERVAL 1 DAY, '缺勤'),
(4, 4, 1, CURDATE() - INTERVAL 1 DAY, '正常');

# OLV Project — New Developer Guide

> For: New team members assigned to this project
> Last updated: 2026-04-30

---

## 1. What Is This Project?

This is a **SaaS (Software as a Service)** government/business web platform built on **eGovFramework** (표준프레임워크) — the Korean public sector standard framework. It has two portals:

| Portal | Module | URL (WAR) | Purpose |
|--------|--------|-----------|---------|
| Admin Portal | `olv-oper` | `adlunch.war` (port 8081) | Managers manage users, content, settings |
| User Portal | `olv-pfom` | `lunch.war` (port 8080) | Business users apply for services |
| REST API | `olv-api` | `apilunch.war` | External API for 3rd-party integration |
| Common Library | `olv-core` | (not deployed alone) | Shared code used by all modules |

---

## 2. Technology Stack — What You Must Know

### 2.1 Backend (Java / Spring)

| Technology | Version | What It Does |
|-----------|---------|-------------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.3.5 | Application framework (auto-config, embedded Tomcat) |
| **eGovFramework RTE** | 5.0.0 | Korean gov standard layer on top of Spring |
| **Spring Security** | 6.x | Login, session, access control |
| **MyBatis** | 3.0.3 | SQL mapper framework (NOT JPA/Hibernate) |
| **Gradle** | Multi-module | Build tool |
| **HikariCP** | (bundled) | Database connection pool |

**Key things to learn about Spring Boot:**
- `@Controller` — handles HTTP requests, returns view name (HTML)
- `@ResponseBody` — returns JSON directly (used for AJAX)
- `@RequestMapping` — maps URL to method
- `@ModelAttribute` — binds form data to Java object
- `@RequestBody` — binds JSON body to Java object
- `@Autowired` — dependency injection
- `@Service`, `@Mapper` — component annotations
- `ModelMap` — passes data from controller to HTML template

**Key things to learn about MyBatis:**
- No auto-generated SQL — you write every SQL query manually in XML files
- `@Mapper` interface methods map 1-to-1 with SQL XML `<select>`, `<insert>`, `<update>`, `<delete>` tags
- `#{paramName}` — safe parameter binding (prevents SQL injection)
- `${}` — NEVER use this (SQL injection risk, forbidden in this project)
- `resultMap` — maps DB column names (snake_case) to Java field names (camelCase)

### 2.2 Database

| Technology | Details |
|-----------|---------|
| **PostgreSQL** | Main database (only DB used in this project) |
| **HikariCP** | Connection pool (max 5 connections in local) |
| Local connection | `jdbc:postgresql://localhost:5432/testdb` |

**Key PostgreSQL patterns used in this project:**
```sql
-- Sequence for PK (instead of AUTO_INCREMENT)
nextval('bbs_seq')

-- Pagination (standard SQL)
LIMIT 10 OFFSET 0

-- Date formatting
TO_CHAR(data_reg_dt, 'YYYY-MM-DD')
TO_DATE('2026.04.30', 'YYYY.MM.DD')

-- Safe string search
bbs_ttl_nm LIKE '%' || #{keyword} || '%'

-- Logical delete (DO NOT use physical DELETE)
UPDATE table SET del_yn = 'Y' WHERE id = #{id}
```

**Common table naming patterns:**
- `co_` prefix = common/shared tables
- `del_yn` column = `'Y'` means deleted, `'N'` means active (logical delete)
- `data_reg_id` = who created the record
- `data_reg_dt` = when it was created
- `data_chg_id` = who last updated
- `data_chg_dt` = when it was last updated

**Things to practice in PostgreSQL:**
- SELECT with WHERE, ORDER BY, GROUP BY
- INSERT with sequences (`nextval`)
- UPDATE / DELETE
- JOIN (INNER JOIN, LEFT JOIN)
- Window functions: `ROW_NUMBER() OVER (ORDER BY ...)`
- Date functions: `TO_CHAR`, `TO_DATE`, `NOW()`

### 2.3 Frontend (HTML / JavaScript)

| Technology | What It Does |
|-----------|-------------|
| **Thymeleaf** | Server-side HTML template engine (Java's version of JSP) |
| **Thymeleaf Layout Dialect** | Shared layout/header/footer across pages |
| **jQuery** | JavaScript library for DOM manipulation and AJAX |
| **jQuery UI Datepicker** | Date picker UI component |
| **IBSheet 8** | Grid/table UI component (paid Korean product) |
| **CommAjax** | Custom project AJAX wrapper class |
| **commPaging** | Custom project pagination helper |

**Key Thymeleaf syntax you must know:**
```html
<!-- Link URL (always use this, never hardcode) -->
<a th:href="@{/pfm/list.do}">link</a>

<!-- Form action -->
<form th:action="@{/pfm/insert.do}" method="post">

<!-- Bind form object -->
<form th:object="${myVO}">
  <input th:field="*{fieldName}"/>   <!-- renders name + value -->
</form>

<!-- Display value (safe, escapes HTML) -->
<span th:text="${vo.name}"></span>

<!-- Conditional -->
<div th:if="${list != null}">...</div>

<!-- Loop -->
<tr th:each="item : ${list}">
  <td th:text="${item.title}"></td>
</tr>

<!-- Include layout -->
<html layout:decorate="~{layout/default}">
<div layout:fragment="content">...</div>
```

**Rules: NEVER use these in Thymeleaf (will break):**
- `href="/..."` hardcoded → use `th:href="@{/...}"`
- `th:utext` → use `th:text` (XSS risk)
- `#request`, `#session` in templates → removed in Thymeleaf 3.1

**JavaScript patterns in this project:**
```javascript
// _ctx = context path (empty locally, "/lunch" on production)
var _ctx = (document.querySelector('meta[name="_ctx"]')?.content || "").replace(/\/$/, "");

// CommAjax — how every AJAX call works
var commAjax = new CommAjax();
commAjax.setUrl(_ctx + "/domain/selectList.do");
commAjax.setForm("searchForm");        // reads form inputs as search params
commAjax.setPaging(paging);            // attach pagination
commAjax.setPagingInit(true);          // true = go to page 1, false = stay on current page
commAjax.setCallback(function(data) {
    // data.body = result list (array)
    // data.page.rowcount = total count
    // data.page.pageindex = current page
});
commAjax.ajax();

// commPaging — pagination UI
var paging = commPaging();
paging.setRowUnit(10);          // 10 rows per page
paging.setSearchFn(fn_search);  // called when page number is clicked
```

**IBSheet 8 — Grid component:**
```javascript
// Create grid
IBSheet.create({
    id: "mySheet",
    el: "container-div-id",
    options: {
        Cols: [
            {Header: "컬럼명", Name: "fieldName", Type: "Text", Width: 100}
        ],
        Events: {
            onAfterClick: function(row) {
                // row click handler
            }
        }
    }
});

// Load data
mySheet.loadSearchData({"data": dataArray});

// Get row value
var rowData = mySheet.getRowValue(row);
```

---

## 3. Project File Structure — Where Everything Goes

```
saas-olv/
├── olv-core/           ← SHARED library (DO NOT modify without permission)
│   └── src/main/java/egovframework/com/cmm/
│       ├── CmmVO.java                      ← Base VO (search, paging, audit fields)
│       ├── LoginVO.java                    ← Logged-in user info
│       ├── CmmMessageSource.java           ← i18n messages
│       ├── CmmProfile.java                 ← Environment (loc/dev/prd)
│       ├── context/SessionHolder.java      ← Current user from ThreadLocal
│       ├── service/CmmInVO.java            ← AJAX request wrapper
│       ├── service/CmmOutVO.java           ← AJAX response wrapper
│       ├── service/CmmPaginationInfo.java  ← Paging calculation
│       ├── service/FileVO.java             ← File attachment
│       ├── exception/CmmBizException.java  ← Business error (HTTP 400)
│       └── exception/CmmException.java     ← System error (HTTP 500)
│
├── olv-oper/           ← Admin portal (YOUR main workspace)
│   ├── src/main/java/egovframework/com/{domain}/
│       ├── web/{Domain}Controller.java
│       ├── service/{Domain}InVO.java
│       ├── service/{Domain}OutVO.java
│       ├── service/{Domain}Service.java
│       ├── service/impl/{Domain}ServiceImpl.java
│       └── mapper/{Domain}Mapper.java
│   └── src/main/resources/
│       ├── templates/egovframework/com/{domain}/   ← HTML files
│       └── egovframework/mapper/com/{domain}/      ← SQL XML files
│
├── olv-pfom/           ← User portal
└── olv-api/            ← REST API
```

---

## 4. How a Feature Is Built (Standard Flow)

When your manager assigns a task like "build X management screen", follow this order:

```
Step 1: Check DB table and columns
Step 2: InVO.java     (extends CmmVO — for input/search/form binding)
Step 3: OutVO.java    (implements Serializable — for query results)
Step 4: SQL XML       (CRUD queries with resultMap)
Step 5: Mapper.java   (@Mapper interface matching SQL XML)
Step 6: Service.java  (interface — declare CRUD methods)
Step 7: ServiceImpl   (@Service — call mapper methods)
Step 8: Controller    (@Controller — URL mapping, call service)
Step 9: HTML files    (List, Regist, Updt)
```

**Always build bottom-up (DB → Java → HTML) to avoid compile errors.**

---

## 5. Common Classes You Will Use Every Day

### InVO (input object)
```java
public class MyFeatureInVO extends CmmVO {
    // CmmVO already provides:
    // - searchCondition, searchKeyword (search)
    // - pageIndex, recordCountPerPage, firstIndex (paging)
    // - dataRegId, dataRegIpAddr, dataChgId, dataChgIpAddr (audit)

    private String myField;   // add your table columns here

    public String getMyField() { return myField; }
    public void setMyField(String myField) { this.myField = myField; }
}
```

### OutVO (output object)
```java
public class MyFeatureOutVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String myField;
    private String dataRegId;
    private String dataRegDt;
    // ... query result fields only

    // getters and setters (NO Lombok — write manually)
}
```

### Setting audit info (who created/updated)
```java
// On INSERT — sets dataRegId and dataRegIpAddr
SessionHolder.setAuditInfo(inVO);

// On UPDATE/DELETE — sets dataChgId and dataChgIpAddr
SessionHolder.setAuditInfoForUpdate(inVO);
```

### Throwing exceptions
```java
// Business logic error (shows user-friendly message)
throw new CmmBizException("M0001", "데이터를 찾을 수 없습니다.");

// System/technical error
throw new CmmException("E0001", "파일 업로드 중 오류가 발생했습니다.", e);
```

### AJAX list endpoint (controller)
```java
@RequestMapping("/myDomain/selectList.do")
@ResponseBody
public CmmOutVO selectList(@RequestBody CmmInVO<MyInVO> request) throws Exception {
    CmmPaginationInfo paginationInfo = CmmPaginationInfo.setupPaging(request);
    MyInVO inVO = request.getBody();

    List<MyOutVO> list = myService.selectList(inVO);
    int totCnt = myService.selectListTotCnt(inVO);
    paginationInfo.setTotalRecordCount(totCnt);

    return CmmOutVO.of(list, paginationInfo);
}
```

---

## 6. SQL XML Patterns You Must Know

### resultMap — maps DB columns to Java fields
```xml
<resultMap id="myFeature" type="egovframework.com.myDomain.service.MyOutVO">
    <result property="myId"      column="my_id"/>
    <result property="myTitle"   column="my_title"/>
    <result property="dataRegId" column="data_reg_id"/>
    <result property="dataRegDt" column="data_reg_dt"/>
</resultMap>
```

### SELECT list with search + paging
```xml
<select id="selectList" parameterType="...InVO" resultMap="myFeature">
    SELECT my_id, my_title, data_reg_id,
           TO_CHAR(data_reg_dt, 'YYYY-MM-DD') AS data_reg_dt
      FROM my_table
     WHERE del_yn = 'N'
    <if test="searchKeyword != null and searchKeyword != ''">
       AND my_title LIKE '%' || #{searchKeyword} || '%'
    </if>
     ORDER BY my_id DESC
     LIMIT #{recordCountPerPage} OFFSET #{firstIndex}
</select>
```

### INSERT with sequence
```xml
<insert id="insert" parameterType="...InVO">
    INSERT INTO my_table (my_id, my_title, del_yn, data_reg_id, data_reg_dt)
    VALUES (nextval('my_seq'), #{myTitle}, 'N', #{dataRegId}, NOW())
</insert>
```

### Logical DELETE (never physical delete)
```xml
<update id="delete" parameterType="...InVO">
    UPDATE my_table
       SET del_yn = 'Y', data_chg_id = #{dataChgId}, data_chg_dt = NOW()
     WHERE my_id = #{myId}
</update>
```

---

## 7. HTML Page Structure

### Layout
Every page uses the shared layout:
```html
<html layout:decorate="~{layout/default}">
<div layout:fragment="content">
    <!-- your page content here -->
</div>
</html>
```

### 3-Page pattern for every feature

| Page | File | Purpose |
|------|------|---------|
| List | `{Domain}List.html` | Search + IBSheet grid + pagination |
| Register | `{Domain}Regist.html` | Create new record form |
| Update/Detail | `{Domain}Updt.html` | View + edit + delete existing record |

### Update page must have these:
```html
<!-- 1. Hidden PK so update knows which row to change -->
<input type="hidden" th:field="*{myId}"/>

<!-- 2. Separate delete form (delete only sends PK) -->
<form id="deleteForm" th:action="@{/myDomain/delete.do}" method="post" hidden>
    <input type="hidden" name="myId" th:value="${myVO?.myId}"/>
</form>

<!-- 3. Delete button calls confirm first -->
<button type="button" onclick="fn_delete()">삭제</button>

<script>
function fn_delete() {
    if (confirm("삭제하시겠습니까?")) {
        document.getElementById("deleteForm").submit();
    }
}
</script>
```

---

## 8. Security & URL Rules

### Static resources — always include `/static/`
```html
<!-- CORRECT -->
<link th:href="@{/static/css/style.css}" rel="stylesheet"/>
<script th:src="@{/static/js/common.js}"></script>

<!-- WRONG — will break in production -->
<link href="/css/style.css" rel="stylesheet"/>
```

### URL context path — always use `@{/...}` in HTML
```html
<!-- CORRECT -->
<a th:href="@{/myDomain/list.do}">목록</a>
<form th:action="@{/myDomain/insert.do}" method="post">

<!-- WRONG — will break when deployed (context path /lunch or /adlunch) -->
<a href="/myDomain/list.do">목록</a>
```

### URL in JavaScript — use `_ctx`
```javascript
// CORRECT
location.href = _ctx + "/myDomain/detail.do?id=" + id;
$.ajax({ url: _ctx + "/myDomain/selectList.do" });

// WRONG
location.href = "/myDomain/detail.do?id=" + id;
```

---

## 9. Project Environments

| Env | `-Dsys` value | Database | Notes |
|-----|--------------|----------|-------|
| Local | `loc` | localhost:5432 | Your development PC |
| Development | `dev` | Dev server DB | Shared dev environment |
| Production | `prd` | Production DB | Live system |

`application.yml` is **git-ignored** (contains DB passwords). You get it separately from your team lead.

---

## 10. Development Rules (DO / DON'T)

### DO
- Use `#{}` for all SQL parameters
- Use `th:href`, `th:src`, `th:action` for all URLs
- Use `th:text` to display values
- Extend `CmmVO` for InVO, implement `Serializable` for OutVO
- Write all getters/setters manually (no Lombok)
- Use `SessionHolder.setAuditInfo()` to record who created a record
- Use logical delete (`del_yn = 'Y'`) instead of physical DELETE
- Throw `CmmBizException` for business errors, `CmmException` for system errors

### DON'T
- Never use `${}` in SQL (SQL injection)
- Never use `th:utext` in HTML (XSS)
- Never hardcode URLs like `href="/..."` (breaks in production)
- Never modify `olv-core` without team lead approval
- Never add new dependencies without team lead approval
- Never use Lombok
- Never use DAO pattern — only `@Mapper` interface

---

## 11. Checklist When Assigned a New Task

```
[ ]  1. Read the design document / wireframe from your manager
[ ]  2. Identify which DB table(s) are involved — ask if unsure
[ ]  3. Create InVO (extends CmmVO)
[ ]  4. Create OutVO (implements Serializable)
[ ]  5. Write SQL XML (selectList, selectListTotCnt, selectDetail, insert, update, delete)
[ ]  6. Create Mapper interface (method names must match SQL XML ids)
[ ]  7. Create Service interface
[ ]  8. Create ServiceImpl
[ ]  9. Create Controller
[ ] 10. Create HTML (List, Regist, Updt)
[ ] 11. Test each operation: search, create, read, update, delete
[ ] 12. Check pagination works correctly
[ ] 13. Check search filters work correctly
[ ] 14. Check validation (empty required fields show alert)
[ ] 15. Check delete shows confirm dialog
```

---

## 12. Things to Study/Practice (Priority Order)

1. **SQL (PostgreSQL)** — Most of your work is writing SQL queries. Practice SELECT, INSERT, UPDATE, GROUP BY, JOIN, subqueries.
2. **Spring MVC** — Understand `@Controller`, `@RequestMapping`, `ModelMap`, redirect pattern.
3. **MyBatis** — Understand how Mapper interface + XML work together, resultMap, dynamic SQL (`<if>`, `<foreach>`).
4. **Thymeleaf** — Practice `th:each`, `th:if`, `th:field`, `th:object`, `layout:decorate`.
5. **jQuery / AJAX** — Understand `$.ajax()`, form serialization, JSON handling.
6. **Java basics** — Getters/setters, inheritance (`extends`), interfaces (`implements`), exception handling.
7. **Git** — Branch, commit, push, pull request flow used by your team.

---

## 13. Sample Code Reference (Always Check This First)

Before writing any new feature, look at the **SmpBoard** sample files:

| File | Location |
|------|----------|
| Controller | `olv-oper/.../smp/web/SmpBoardController.java` |
| InVO | `olv-oper/.../smp/service/SmpBoardInVO.java` |
| OutVO | `olv-oper/.../smp/service/SmpBoardOutVO.java` |
| Service | `olv-oper/.../smp/service/SmpBoardService.java` |
| ServiceImpl | `olv-oper/.../smp/service/impl/SmpBoardServiceImpl.java` |
| Mapper | `olv-oper/.../smp/mapper/SmpBoardMapper.java` |
| SQL XML | `olv-oper/.../resources/egovframework/mapper/com/smp/SmpBoard_SQL.xml` |
| List HTML | `olv-oper/.../templates/egovframework/com/smp/SmpBoardList.html` |
| Register HTML | `olv-oper/.../templates/egovframework/com/smp/SmpBoardRegist.html` |
| Update HTML | `olv-oper/.../templates/egovframework/com/smp/SmpBoardUpdt.html` |
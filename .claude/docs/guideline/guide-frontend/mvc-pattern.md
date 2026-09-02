# 개발 가이드 — 프론트엔드 MVC 패턴 (전역 함수 / jQuery 플러그인 / 객체 리터럴)

> **적용 대상:** WAR 패키징 프로젝트 + jQuery 플러그인 패턴 적용 Spring Boot Web(JAR) 프로젝트
> **단일 출처:** `.claude/config/project.yaml` `projects[]` (`common.md` §1 기술 스택 표 참조)
>
> 공통 규칙(디렉토리, AJAX, DOM, 라이브러리, Thymeleaf, CSS, 보안)은 `common.md` 를 참조한다.

---

## 스코프 → 패턴 매핑

> 프로젝트별 스코프 매핑(어느 프로젝트가 어느 패턴을 쓰는지)은 **프로젝트 프런트엔드 가이드**를 참조한다.

본 가이드가 다루는 패턴:

| JS 패턴                                       | 특징                                      | 적용 섹션 |
| --------------------------------------------- | ----------------------------------------- | --------- |
| 전역 함수 + 프로젝트 AJAX 래퍼                | `$(document).ready` 후 전역 함수 정의     | §4-1, §7-1 |
| jQuery 플러그인 IIFE + 프로젝트 AJAX 라이브러리 | `(function($){...})(jQuery)` 래핑        | §4-2 |
| 객체 리터럴 네임스페이스 + 공통 라이브러리 + UI 컴포넌트 | 단일 객체에 메서드 정의                   | §4-3 |

---

## 4. JavaScript 코딩 패턴 (프로젝트별)

### 4-1. 전역 함수 패턴 — 전역 함수 + 프로젝트 AJAX 래퍼

```javascript
$(document).ready(function () {
  initPage();
  bindEvents();
});

/**
 * 페이지 초기화 처리.
 */
function initPage() {
  // 초기 데이터 로드, UI 상태 설정
}

/**
 * 이벤트 바인딩 처리.
 */
function bindEvents() {
  $("#btnSearch").on("click", function () {
    searchList();
  });
}

/**
 * 목록 조회 AJAX 호출.
 */
function searchList() {
  projectAjax("/api/user/list", "POST", $("#searchForm").serialize(), {
    200: function (data) {
      renderList(data);
    },
    422: function (data) {
      alert(data.message);
    },
    500: function () {
      alert("서버 오류가 발생했습니다.");
    },
  });
}
```

### 4-2. jQuery 플러그인 패턴 — jQuery 플러그인 + 프로젝트 AJAX 라이브러리

```javascript
(function ($) {
  "use strict";

  /**
   * 페이지 초기화.
   */
  $(document).ready(function () {
    initPage();
    bindEvents();
  });

  function initPage() {
    // 초기화 로직
  }

  function bindEvents() {
    $("#btnSearch").on("click", searchList);
  }

  /**
   * 목록 조회.
   */
  function searchList() {
    projectAjax({
      url: "/api/merchant/list",
      type: "POST",
      data: $("#searchForm").serialize(),
      success: function (res) {
        if (res.resultCode === "0000") {
          renderList(res.data);
        } else {
          alert(res.resultMsg);
        }
      },
      error: function () {
        alert("서버 오류가 발생했습니다.");
      },
    });
  }
})(jQuery);
```

### 4-3. 객체 리터럴 네임스페이스 패턴 — 객체 리터럴 + AJAX 라이브러리 + UI 컴포넌트

```javascript
/**
 * 사용자 목록 페이지 모듈.
 */
const userList = {
  /**
   * 초기화.
   */
  init() {
    this.bindEvents();
    this.loadList();
  },

  /**
   * 이벤트 바인딩.
   */
  bindEvents() {
    $("#btnSearch").on("click", () => this.loadList());
    $("#btnRegister").on("click", () => this.openRegisterModal());
  },

  /**
   * 목록 조회.
   */
  loadList() {
    projectAjax({
      url: "/api/user/list",
      type: "POST",
      data: $("#searchForm").serialize(),
      success: (res) => {
        if (res.resultCode === "0000") {
          this.renderList(res.data);
        } else {
          uiModal.alert(res.resultMsg);
        }
      },
      error: () => {
        uiModal.alert("서버 오류가 발생했습니다.");
      },
    });
  },

  /**
   * 목록 렌더링.
   *
   * @param {Array} list - 조회된 목록 데이터
   */
  renderList(list) {
    // DOM 렌더링 로직
  },
};

$(document).ready(function () {
  userList.init();
});
```

---

## 7. 오류 처리

### 7-1. 전역 함수 패턴 — statusCode 기반 오류 처리

```javascript
projectAjax("/api/action", "POST", data, {
  200: function (data) {
    // 성공 처리
  },
  422: function (data) {
    alert(data.message || "입력 값을 확인해 주세요.");
  },
  500: function () {
    alert("서버 오류가 발생했습니다.");
  },
});
```

> §7-2 응답 코드 기반 오류 처리(jQuery 플러그인·객체 리터럴·IIFE 패턴 적용 프로젝트)는 `common.md` 참조.

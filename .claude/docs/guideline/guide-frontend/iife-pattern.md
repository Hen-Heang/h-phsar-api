# 개발 가이드 — 프론트엔드 IIFE 패턴 (UI 네임스페이스)

> **적용 대상:** IIFE + `UI` 네임스페이스 패턴 적용 Spring Boot Web(JAR) 프로젝트
> **단일 출처:** `.claude/config/project.yaml` `projects[]` (`common.md` §1 기술 스택 표 참조)
>
> 공통 규칙(디렉토리, AJAX, DOM, 라이브러리, Thymeleaf, CSS, 보안)은 `common.md` 를 참조한다.

---

## 스코프 → 패턴 매핑

> 프로젝트별 스코프 매핑(어느 프로젝트가 어느 패턴을 쓰는지)은 **프로젝트 프런트엔드 가이드**를 참조한다.

본 가이드가 다루는 패턴:

| JS 패턴                                       | 적용 섹션 |
| --------------------------------------------- | --------- |
| IIFE + `UI` 네임스페이스                      | §4-4 |
| IIFE + `UI` 네임스페이스 + ES6 class 허용     | §4-4, §7-3 |

> **구별 기준**: 본 패턴은 단순 IIFE 래핑이 아닌 **프로젝트 UI 네임스페이스 사용**(`UI.modal.alert(...)` 등)이 핵심이다. 프로젝트 UI 네임스페이스 미사용 프로젝트는 `mvc-pattern.md` 를 따른다.

---

## 4. JavaScript 코딩 패턴

### 4-4. IIFE + `UI` 네임스페이스 패턴

```javascript
(() => {
  "use strict";

  const PAGE_SIZE = 10;

  /**
   * 페이지 초기화.
   */
  function init() {
    bindEvents();
    loadData();
  }

  /**
   * 이벤트 바인딩.
   */
  function bindEvents() {
    $("#btnSearch").on("click", loadData);
  }

  /**
   * 데이터 조회.
   */
  function loadData() {
    $.ajax({
      url: "/api/order/list",
      type: "POST",
      data: JSON.stringify({ pageSize: PAGE_SIZE }),
      contentType: "application/json",
      success(res) {
        if (res.resultCode === "0000") {
          renderData(res.data);
        } else {
          UI.modal.alert(res.resultMsg);
        }
      },
      error() {
        UI.modal.alert("서버 오류가 발생했습니다.");
      },
    });
  }

  $(document).ready(init);
})();
```

**ES6 class 패턴** (ES6 class 허용 프로젝트):

```javascript
(() => {
  "use strict";

  /**
   * 처리 클래스 (ES6 class 예시).
   */
  class ItemProcessor {
    /**
     * @param {string} itemId - 처리 대상 ID
     */
    constructor(itemId) {
      this.itemId = itemId;
      this.$form = $("#processForm");
    }

    /**
     * 처리 요청을 전송한다.
     */
    submit() {
      const formData = this.$form.serialize();
      $.ajax({
        url: "/api/order/process",
        type: "POST",
        data: formData,
        success: (res) => this.handleResponse(res),
        error: () => UI.modal.alert("처리 중 오류가 발생했습니다."),
      });
    }

    /**
     * 응답 처리.
     *
     * @param {Object} res - 서버 응답
     */
    handleResponse(res) {
      if (res.resultCode === "0000") {
        UI.modal.alert("처리가 완료되었습니다.", () => {
          location.href = "/item/complete";
        });
      } else {
        UI.modal.alert(res.resultMsg);
      }
    }
  }

  $(document).ready(function () {
    const processor = new ItemProcessor(_itemId_);
    $("#btnSubmit").on("click", () => processor.submit());
  });
})();
```

---

## 7. 오류 처리

### 7-3. IIFE + ES6 async — try-catch 패턴

```javascript
async function processOrder(orderData) {
  try {
    const res = await $.ajax({
      url: "/api/order/process",
      type: "POST",
      data: JSON.stringify(orderData),
      contentType: "application/json",
    });
    if (res.resultCode === "0000") {
      return res.data;
    }
    UI.modal.alert(res.resultMsg);
    return null;
  } catch (error) {
    UI.modal.alert("주문 처리 중 오류가 발생했습니다.");
    return null;
  }
}
```

> §7-2 응답 코드 기반 오류 처리(IIFE + UI 네임스페이스 패턴 프로젝트 중 AJAX 라이브러리 사용 스코프)는 `common.md` 참조.

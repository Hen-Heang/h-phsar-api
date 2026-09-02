# code-investigator search profiles

Read only when `workType=frontend|fullstack|unknown`. The Core body of this file defines only the profile-selection contract; installed pack fragments append the concrete framework paths, extensions, and paired-file rules below.

1. If the target project's `projects[].guideline.frontend` exists, read it first and prefer the project's own paths and conventions. If absent, apply only the installed pack profiles.
2. Apply only the installed profiles that match the current project. If several profiles match, apply them all.
3. If neither a project frontend guideline nor an installed pack profile exists, do not guess arbitrary framework paths — record `조립된 프런트 프로필 없음` in the result.
4. Collect screen evidence in this order: similar screens, paired scripts/styles, event/data bindings, called APIs.

## jQuery·정적 웹 프로필

- 대상: `*.html`, `*.htm`, `*.js`, `*.css`와 프로젝트 프런트 가이드가 지정한 템플릿·정적 리소스 경로.
- 연결: 같은 화면명·상대경로·script/link include를 기준으로 템플릿↔JS↔CSS를 짝짓는다.
- 동작: 이벤트 바인딩(`on`, `click`, `change`, `submit`), DOM 갱신, AJAX/fetch 호출과 요청·응답 필드명을 확인한다.
- evidence: 동일 UI 패턴 화면 1~2개와 호출 URL·함수·payload 키를 함께 반환한다.

## Spring 서버 렌더링 프로필

- 대상: `src/main/resources/templates/**/*.html`, `src/main/resources/static/**/*`, `src/main/webapp/WEB-INF/**/*.jsp`와 프로젝트 프런트 가이드 경로.
- 연결: 템플릿의 링크·폼·스크립트 include와 Controller 매핑, model attribute, 요청 DTO를 대조한다.
- evidence: 화면 필드↔요청 필드, 화면 액션↔Controller/API, 템플릿↔정적 JS/CSS 연결을 반환한다.

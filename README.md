![image](https://github.com/user-attachments/assets/b63a460a-c387-4fae-83c3-44ac6123c311)

1. DB 정보
 - mysql  Ver 8.0.36 for Linux on x86_64

2. Lecture (특강 테이블)
  테이블 설명:
   - Lecture 테이블은 각 특강에 대한 정보를 저장합니다.
  
  필드 설명:
 - id: 각 특강의 고유 식별자. (Primary Key, auto_increment)
 - title: 특강 제목.
 - lecturer: 강사 이름.
 - date: 특강이 진행되는 날짜.
 - max_capacity: 최대 수강 가능 인원. 기본값은 30명입니다.
 - current_applicants: 현재 신청 인원. 기본값은 0으로 설정됩니다.
 - created_at: 특강 생성 시간. 기본값으로 CURRENT_TIMESTAMP가 설정됩니다.
 - updated_at: 특강 정보 수정 시간. 특강이 수정될 때마다 CURRENT_TIMESTAMP로 자동 업데이트됩니다.

3. UserLecture (특강 신청 기록 테이블)
  테이블 설명:
    - UserLecture 테이블은 각 사용자가 어떤 특강에 신청했는지 기록합니다. 이는 사용자와 특강 간의 다대다 관계를 관리하는 조인 테이블 역할을 합니다.
  필드 설명:
 - id: 각 신청 기록의 고유 식별자. (Primary Key, auto_increment)
 - lecture_id: 신청한 특강의 ID. (Foreign Key)
 - user_id: 특강을 신청한 사용자의 ID. (Foreign Key)
 - created_at: 신청 시간이 자동으로 저장됩니다.

4. Users (사용자 테이블)
  테이블 설명:
    - Users 테이블은 특강 신청 시스템에 가입한 사용자의 정보를 저장합니다.
  필드 설명:
 - id: 각 사용자의 고유 식별자. (Primary Key, auto_increment)
 - username: 사용자 이름. 10자리 이하의 문자로 제한되며 중복을 허용하지 않습니다.
 - password: 사용자의 비밀번호. 해시된 상태로 저장됩니다.

5. 설계 이유
 - Lecture 테이블: 각 특강에 대한 기본 정보와 수강 신청 인원 관리를 위해 설계되었습니다. max_capacity와 current_applicants 필드가 있어 특강 신청 인원을 제한하고 관리할 수 있습니다.
 - UserLecture 테이블: 사용자와 특강 간의 다대다 관계를 관리하기 위한 중간 테이블입니다. lecture_id와 user_id를 외래 키로 사용하여 신청 내역을 기록하고 추적할 수 있습니다.
 - Users 테이블: 사용자 정보를 관리하는 테이블로, 사용자 이름과 비밀번호를 저장하며, 사용자 이름에 대한 중복을 허용하지 않기 위해 username 필드에 UNIQUE 제약을 적용했습니다.

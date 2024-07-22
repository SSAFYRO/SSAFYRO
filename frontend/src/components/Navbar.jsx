export default function Navbar() {
  return (
    <>
      <nav className="flex py-3 px-4 text-lg">
        {/* 로고 자리 */}
        <div className="flex-auto text-start">
          <a className="font-extrabold" href="/">
            <img className="h-[24px] inline pr-2" src="/SSAFYRO.png" alt="" />
            SSAFYRO
          </a>
        </div>
        {/* 메뉴 자리 */}
        <div className="flex-auto text-center font-semibold">
          <a className="mx-3" href="/first">
            1차
          </a>
          <a className="mx-3" href="/second/guide/personality">
            2차
          </a>
        </div>
        {/* 회원 메뉴 */}
        <div className="flex-auto text-end font-semibold">
          <a href="/account/login">로그인</a>
        </div>
      </nav>
    </>
  );
}

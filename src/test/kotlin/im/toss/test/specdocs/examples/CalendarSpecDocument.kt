package im.toss.test.specdocs.examples

import im.toss.test.specdocs.*

class CalendarSpecDocument: AbstractSpecFromTestReporter() {

    override fun baseUri() = ""

    override fun pathToSpecDocument() = "build/reports/specs/Calendar.md"

    override fun render(baseUri: String, root: Node): String {
        val specListGenerator = SpecListGenerator(baseUri)
        val specTableOfContentsGenerator = SpecTableOfContentsGenerator()

        val target = root.findById(CalendarSpecTest::class.simpleName!!)!!

        return """
달력의 규칙

${specTableOfContentsGenerator.generate(target)}
## 그레고리력

> 그레고리력(Gregorian calendar)은 현재 세계적으로 통용되는 양력(陽曆)으로, 1582년에 교황 그레고리오 13세가 율리우스력을 개정하여 이 역법을 시행했기 때문에 그레고리력이라고 부른다.
> 
> 율리우스 카이사르가 기원전 46년에 제정한 율리우스력은 4년마다 2월 29일을 추가하는 윤년을 두었는데, 율리우스력의 1년 길이는 365.25일이므로 천문학의 회귀년 365.2422일보다 0.0078일(11분 14초)이 길어서 128년마다 1일의 편차가 났다.
> 
> 그레고리력은 율리우스력의 이러한 단점을 보완한 역법으로, 1582년 10월 4일 교황 그레고리오 13세는 율리우스력의 400년에서 3일(세 번의 윤년)을 없애는 방법으로 이를 해결했다. 그레고리력의 1년 길이는 365.2425일이므로, 천문학의 회귀년보다 0.0003일(26초)이 길고 약 3,300년마다 1일의 편차가 난다.
> 
> -- https://ko.wikipedia.org/wiki/%EA%B7%B8%EB%A0%88%EA%B3%A0%EB%A6%AC%EB%A0%A5

${target.let { it.children.joinToString("\n\n") { specListGenerator.generate(it, 3) } } ?: ""}

### 4의 배수인 해를 윤년로 한다. 그러나 100으로 나눌 수 있지만 400으로 나뉘어 떨어지지 않는 해는 예외로 평년으로 한다.

* 2012년 2월은 29일까지 있다.
* 2000년 2월은 29일까지 있다.
* 1900년 2월은 28일까지 있다.
""".trimIndent()
    }
}

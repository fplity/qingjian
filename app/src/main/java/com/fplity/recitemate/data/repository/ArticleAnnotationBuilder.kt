package com.fplity.recitemate.data.repository

import com.fplity.recitemate.data.model.AnnotatedSentence
import com.fplity.recitemate.data.model.AnnotatedWord

/**
 * Keeps interaction data separate from the display strings. IDs are based on
 * article, sentence and occurrence, so repeated words never collide.
 */
internal object ArticleAnnotationBuilder {
    private data class Gloss(val source: String, val meaning: String)

    private val glossary = listOf(
        Gloss("见贤思齐", "看见有德行、有才能的人，就想着向他看齐。"),
        Gloss("文质彬彬", "文采和质朴配合得恰当。"),
        Gloss("任重道远", "责任重大，道路遥远；比喻使命艰巨。"),
        Gloss("朝闻道", "早晨明白了真理或道义。"),
        Gloss("不仁", "没有仁德，不爱人。"),
        Gloss("君子", "这里指有德行、有修养的人。"),
        Gloss("小人", "这里指只顾私利、缺少德行的人。"),
        Gloss("见贤", "看见有德行、有才能的人。"),
        Gloss("内自省", "在内心反省自己的不足。"),
        Gloss("质胜文", "质朴超过文采。"),
        Gloss("文胜质", "文采超过质朴。"),
        Gloss("礼", "礼制、礼仪以及合乎规范的行为。"),
        Gloss("乐", "音乐，也可指由礼乐形成的和谐秩序。"),
        Gloss("仁", "儒家伦理观念，指关爱他人、推己及人。"),
        Gloss("义", "合乎道义、应当遵循的原则。"),
        Gloss("道", "真理、正确的原则或学说。"),
        Gloss("贤", "有德行、有才能的人。"),
        Gloss("人", "人；本句中指做人者。"),
        Gloss("知", "懂得、明白；读“智”时也可指智慧。"),
        Gloss("行", "行动、实行；也可指品行。"),
        Gloss("学", "学习、求学。"),
        Gloss("师", "老师；也可作学习、效法的对象。"),
        Gloss("德", "品德、德行。"),
        Gloss("志", "志向、心意。"),
        Gloss("心", "内心、思想。"),
        Gloss("国", "国家、国事。"),
        Gloss("王", "君主。"),
        Gloss("为", "做、成为；具体含义依句意而定。"),
        Gloss("以", "用、拿；也可表示目的或原因。"),
        Gloss("于", "在、到、从、比、向等，需依句意判断。"),
        Gloss("其", "他的、它的、其中的；也可作语气词。"),
        Gloss("之", "的、它、他；也可作动词“到、往”。"),
        Gloss("而", "表示承接、转折或修饰关系的连词。"),
        Gloss("则", "就、却，表示承接或转折。"),
        Gloss("故", "所以、因此；也可指旧的、缘故。"),
        Gloss("虽", "即使、虽然。"),
        Gloss("若", "像、如；也可表示假设“如果”。"),
        Gloss("可", "可以、能够、值得。"),
        Gloss("无", "没有。"),
        Gloss("不", "不、没有，表示否定。"),
        Gloss("也", "句末语气词，常表示判断或停顿。"),
        Gloss("矣", "句末语气词，常表示完成或感叹。"),
        Gloss("焉", "兼词或代词、语气词，具体义项依句意而定。"),
        Gloss("乎", "句末语气词，常表示疑问、感叹或停顿。"),
        Gloss("者", "……的人、事、物；常与“也”构成判断。")
    ).sortedByDescending { it.source.length }

    private val sentenceTranslations = mapOf(
        "人而不仁，如礼何？" to "一个人没有仁德，怎样谈得上礼呢？",
        "人而不仁，如乐何？" to "一个人没有仁德，怎样谈得上音乐的教化作用呢？",
        "朝闻道，夕死可矣。" to "早晨明白了真理，即使晚上死去也没有遗憾。",
        "君子喻于义，小人喻于利。" to "君子明白的是道义，小人明白的是私利。",
        "见贤思齐焉，见不贤而内自省也。" to "看见贤德的人就想着向他看齐；看见不贤的人便在内心反省自己。",
        "质胜文则野，文胜质则史。" to "质朴超过文采就显得粗野，文采超过质朴就显得浮夸。",
        "文质彬彬，然后君子。" to "文采和质朴配合得当，这才是君子。",
        "士不可以不弘毅，任重而道远。" to "读书人不能不胸怀宽广、意志坚定，因为责任重大、道路遥远。",
        "仁以为己任，不亦重乎？" to "把实行仁道作为自己的责任，不是很重大吗？",
        "死而后已，不亦远乎？" to "到死才停止，不是很遥远吗？",
        "譬如为山，未成一篑，止，吾止也；" to "譬如堆土成山，只差一筐土却停下，这是我自己停下的。",
        "譬如平地，虽覆一篑，进，吾往也。" to "譬如在平地上堆土，即使只倒下一筐土而继续前进，也是我自己在前进。",
        "知者不惑，仁者不忧，勇者不惧。" to "智慧的人不迷惑，仁德的人不忧愁，勇敢的人不畏惧。",
        "小子何莫学夫《诗》？" to "学生们为什么不学习《诗经》呢？",
        "《诗》可以兴，可以观，可以群，可以怨。" to "《诗经》可以激发情志、观察社会、与人合群、抒发怨愤。",
        "迩之事父，远之事君；" to "近处可以用它来侍奉父母，远处可以用它来侍奉君主。",
        "多识于鸟兽草木之名。" to "还可以多认识鸟兽草木的名称。",
        "吾尝终日不食，终夜不寝，以思，无益，不如学也。" to "我曾整天不吃、整夜不睡地思考，却没有益处，还不如学习。",
        "君子食无求饱，居无求安，敏于事而慎于言，就有道而正焉，可谓好学也已。" to "君子吃饭不追求饱足，居住不追求安逸，做事勤快、说话谨慎，接近有道德的人来匡正自己，这就可以称为好学了。",
        "三人行，必有我师焉。" to "几个人同行，其中一定有可以做我老师的人。",
        "择其善者而从之，其不善者而改之。" to "选择他们的优点来学习，看到他们的缺点就反省并改正自己。"
    )

    fun build(
        articleId: Int,
        content: List<String>,
        articleTranslation: List<String>
    ): List<AnnotatedSentence> {
        val fallback = articleTranslation.firstOrNull()
            ?: "本句的释义正在校对，请结合全文理解。"
        return content.flatMap(::splitSentences).mapIndexed { index, source ->
            val sentenceId = "article-" + articleId + "-sentence-" + (index + 1)
            AnnotatedSentence(
                id = sentenceId,
                translationId = "article-" + articleId + "-translation-" + (index + 1),
                source = source,
                translation = sentenceTranslations[source] ?: fallback,
                words = buildWords(sentenceId, source)
            )
        }
    }

    private fun splitSentences(line: String): List<String> {
        val sentences = mutableListOf<String>()
        val buffer = StringBuilder()
        line.forEach { char ->
            buffer.append(char)
            if (char in sentenceEndings) {
                sentences += buffer.toString().trim()
                buffer.clear()
            }
        }
        buffer.toString().trim().takeIf(String::isNotEmpty)?.let(sentences::add)
        return sentences
    }

    private fun buildWords(sentenceId: String, source: String): List<AnnotatedWord> {
        val words = mutableListOf<AnnotatedWord>()
        var start = 0
        while (start < source.length) {
            val match = glossary.firstOrNull { source.startsWith(it.source, start) }
                ?: source[start].takeIf { it.isChinese() }?.let { char ->
                    Gloss(char.toString(), "“" + char + "”在本句中的意思请结合整句翻译理解。")
                }
            if (match == null) {
                start++
                continue
            }
            val occurrence = words.size + 1
            val end = start + match.source.length
            words += AnnotatedWord(
                id = sentenceId + "-word-" + occurrence,
                glossId = sentenceId + "-gloss-" + occurrence,
                source = match.source,
                start = start,
                endExclusive = end,
                gloss = match.meaning
            )
            start = end
        }
        return words
    }

    private fun Char.isChinese(): Boolean = this in '\u4e00'..'\u9fff'

    private val sentenceEndings = setOf('。', '！', '？', '；')
}

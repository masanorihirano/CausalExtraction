package extractCausal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import utilities.FileUtilities;
import utilities.StringUtilities;
import cabochaParser.CabochaParser;
import cabochaParser.ExecCabocha;
import cabochaParser.CabochaParser.*;
import extractCausal.Causal;
import extractCausal.CausalExtraction;

public class CausalExtractionTest {
	String[] demonList = FileUtilities.readLines("src/extractCausal/demonstrative_list.txt");
	ArrayList<String[]> clueList = FileUtilities.readClueList("src/extractCausal/clue_list.txt");
	
	CausalExtraction ce = new CausalExtraction(clueList, demonList);
	CabochaParser parser = new CabochaParser();


	@Test
	public void testRemoveKoto() {
		String str;

		str = this.ce.removeKoto("ほげほげの");
		assertThat("ほげほげ", is(str));

		str = this.ce.removeKoto("ほげほげなどの");
		assertThat("ほげほげ", is(str));

		str = this.ce.removeKoto("ほげほげ");
		assertThat("ほげほげ", is(str));

		str = this.ce.removeKoto("ほげほげことのなど等");
		assertThat("ほげほげ", is(str));
	}

	@Test
	public void testIncludeDemon() {
		assertThat(false, is(this.ce.includeDemon("あたなのために")));
		assertThat(false, is(this.ce.includeDemon("私それで")));
		assertThat(true, is(this.ce.includeDemon("そのため")));
		assertThat(true, is(this.ce.includeDemon("それで")));
	}

	@Test
	public void testGetCoreIds() throws IOException, InterruptedException {
		String str = StringUtilities.join("\n", ExecCabocha.exec("円高のため、不況になった。"));
		ArrayList<POS> caboList = parser.parse(str);
		assertThat(new Integer[]{1}, is(this.ce.getCoreIds(caboList, "ため、")));

		assertThat(new Integer[]{}, is(this.ce.getCoreIds(caboList, "によって、")));

		str = StringUtilities.join("\n", ExecCabocha.exec("円高のため、不況になったため、損した。"));
		caboList = parser.parse(str);
		assertThat(new Integer[]{1, 4}, is(this.ce.getCoreIds(caboList, "ため、")));
	}

	@Test
	public void testRemoveParticle() throws Exception {
		String str = StringUtilities.join("\n", ExecCabocha.exec("円高のため、不況になったため、損した。"));
		ArrayList<POS> caboList = parser.parse(str);
		assertThat("円高", is(this.ce.removeParticle(caboList.get(0))));
		assertThat("ため", is(this.ce.removeParticle(caboList.get(1))));
		assertThat("不況", is(this.ce.removeParticle(caboList.get(2))));
		assertThat("なった", is(this.ce.removeParticle(caboList.get(3))));
		assertThat("ため", is(this.ce.removeParticle(caboList.get(4))));
		assertThat("損した。", is(this.ce.removeParticle(caboList.get(5))));
	}

	@Test
	public void testGetResultVP() throws Exception {
		String clue = "で、";
		String sentence = "円高による不況の影響で、買い物客が激減した。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("買い物客が激減した。", is(this.ce.getResultVP(caboList, clue, 2)));

		clue = "ため、";
		sentence = "十分なデータの蓄積がなく、合理的な見積もりが困難であるため、権利行使期間の中間点において行使されるものと想定して見積もっております。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("権利行使期間の中間点において行使される", is(this.ce.getResultVP(caboList, clue, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "により";
		sentence = "食品業界で、景気後退に伴う消費マインドの冷え込みや、生活防衛による購買単価の落ち込みなどにより企業業績の後退を余儀なくされ、企業間競争はますます熾烈さを増してまいりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("企業業績の後退を余儀なくされ", is(this.ce.getResultVP(caboList, clue, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "から、";
		sentence = "製菓原材料類は、製菓・製パン向けの販売が総じて低調に推移したことから、各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。", is(this.ce.getResultVP(caboList, clue, this.ce.getCoreIds(caboList, clue)[0])));
	}

	@Test
	public void testGetResultNP() throws Exception {
		String sentence = "円高による不況の影響で、買い物客が激減。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("不況の影響", is(this.ce.getResultNP(caboList, 0)));

		sentence = "円高による不況で、買い物客が激減。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("不況", is(this.ce.getResultNP(caboList, 0)));
	}

	@Test
	public void testGetSubj() throws Exception {
		String clue = "により";
		String sentence = "食品業界で、景気後退に伴う消費マインドの冷え込みや、生活防衛による購買単価の落ち込みなどにより企業業績の後退を余儀なくされ、企業間競争はますます熾烈さを増してまいりました。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("食品業界で、", is(this.ce.getSubj(caboList, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "から、";
		sentence = "製菓原材料類は、製菓・製パン向けの販売が総じて低調に推移したことから、各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("製菓原材料類は、", is(this.ce.getSubj(caboList, this.ce.getCoreIds(caboList, clue)[0])));

		clue = "で、";
		sentence = "国内の生茸の販売は、消費全体が収縮する中で茸の需要も低迷し、価格は平年を下回る厳しい相場で推移したことで、販売量、販売価格ともに前年を割り込む結果となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("国内の生茸の販売は、", is(this.ce.getSubj(caboList, this.ce.getCoreIds(caboList, clue)[0])));
	}

	@Test
	public void testGetKotoResult() throws Exception {
		String sentence = "ブッシュ大統領が二十九日の一般教書演説で雇用を最重視した経済対策を強調したのも、景気回復を確実なものにするには、雇用悪化に歯止めをかける必要があると判断したためだ。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("ブッシュ大統領が二十九日の一般教書演説で雇用を最重視した経済対策を強調したのも、", is(this.ce.getKotoResult(caboList, 6)));

		sentence = "日銀が景気の先行きに慎重なのは、設備投資調整や公共事業の拡大などプラス要因がある半面、雇用調整や円高などマイナス要因も目立ち、「両者がせめぎ合っているのが現状」と見ているためだ。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("日銀が景気の先行きに慎重なのは、", is(this.ce.getKotoResult(caboList, 3)));

		sentence = "配当原資が不足するのは、前期末の有価証券評価差額金が十七億円強の含み損となったため。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat("配当原資が不足するのは、", is(this.ce.getKotoResult(caboList, 1)));
	}

	@Test
	public void testGetBasis() throws Exception {
		String str = StringUtilities.join("\n", ExecCabocha.exec("円高のため、不況になった。"));
		ArrayList<POS> caboList = parser.parse(str);
		assertThat("円高", is(this.ce.getBasis(caboList, "ため、", 1)));
		assertThat("", is(this.ce.getBasis(caboList, "ため、", 3)));

		str = StringUtilities.join("\n", ExecCabocha.exec("十分なデータの蓄積がなく、合理的な見積もりが困難であるため、権利行使期間の中間点において行使されるものと想定して見積もっております。"));
		caboList = parser.parse(str);
		Integer[] coreIds = this.ce.getCoreIds(caboList, "ため、");
		assertThat("十分なデータの蓄積がなく、合理的な見積もりが困難である", is(this.ce.getBasis(caboList, "ため、", coreIds[0])));
		assertThat("", is(this.ce.getBasis(caboList, "ため、", 1)));
	}

	@Test
	public void testGetPatternCFlag() throws Exception {
		String sentence = "ブッシュ大統領が二十九日の一般教書演説で雇用を最重視した経済対策を強調したのも、景気回復を確実なものにするには、雇用悪化に歯止めをかける必要があると判断したためだ。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat(6, is(this.ce.getPatternCFlag(caboList, this.ce.getCoreIds(caboList, "ためだ。")[0])));

		sentence = "日銀が景気の先行きに慎重なのは、設備投資調整や公共事業の拡大などプラス要因がある半面、雇用調整や円高などマイナス要因も目立ち、「両者がせめぎ合っているのが現状」と見ているためだ。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat(3, is(this.ce.getPatternCFlag(caboList, this.ce.getCoreIds(caboList, "ためだ。")[0])));

		sentence = "配当原資が不足するのは、前期末の有価証券評価差額金が十七億円強の含み損となったため。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		assertThat(1, is(this.ce.getPatternCFlag(caboList, this.ce.getCoreIds(caboList, "ため。")[0])));
	}

	@Test
	public void testGetIncludingClues() throws Exception {
		String sentence = "円高を背景に、景気が悪化した。";
		HashMap<String, Integer> result = this.ce.getIncludingClues(sentence, this.ce.clueHash);
		assertThat(result.get("を背景に、"), is(0));
		assertThat(result.get("を背景に"), is(1)); // かぶりがあったら1になる
		
		sentence = "円高を背景に景気が悪化した。";
		result = this.ce.getIncludingClues(sentence, this.ce.clueHash);
		assertThat(result.get("を背景に、"), is(0));
		assertThat(result.get("を背景に"), is(0));
	}

	@Test
	public void testGetCausalExpression() throws Exception {
		String clue = "ため、";
		String sentence = "円高のため、不況になった。";
		ArrayList<POS> caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		Causal causal = this.ce.getCausalExpression(caboList, clue, 1, sentence, "");
		Causal seikai = new Causal("円高", "不況になった。", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));	
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "による";
		sentence = "円高による不況の影響で、買い物客が激減。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, 0, sentence, "");
		seikai = new Causal("円高", "不況の影響", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));	
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));
		
		clue = "による";
		sentence = "この結果による不況の影響で、買い物客が激減。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, 0, sentence, "");
		seikai = new Causal("", "", "", "");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));	
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "から、";
		sentence = "製菓原材料類は、製菓・製パン向けの販売が総じて低調に推移したことから、各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "");
		seikai = new Causal("製菓・製パン向けの販売が総じて低調に推移した", "各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。", "製菓原材料類は、", "B");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));	
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "ため。";
		sentence = "配当原資が不足するのは、前期末の有価証券評価差額金が十七億円強の含み損となったため。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "");
		seikai = new Causal("前期末の有価証券評価差額金が十七億円強の含み損となった", "配当原資が不足するのは、", "", "C");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));	
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "ためだ。";
		sentence = "公共工事と住宅建設が高水準を維持、個人消費も堅調なうえ、設備投資が前年度を上回る見通しとなっているためだ。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "TTT");
		seikai = new Causal("公共工事と住宅建設が高水準を維持、個人消費も堅調なうえ、設備投資が前年度を上回る見通しとなっている", "TTT", "", "D");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));

		clue = "そのため、";
		sentence = "そのため、平成２３年３月期第１四半期の経営成績（累計）及び対前年同四半期増減率については記載しておりません。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "TTT");
		seikai = new Causal("TTT", "平成２３年３月期第１四半期の経営成績（累計）及び対前年同四半期増減率については記載しておりません。", "", "E");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));
		
		clue = "で、";
		sentence = "下半期では、上半期に導入を予定していながら諸事情により計画が遅れた案件の成約が見込めますので、売上高に関しましては上半期の不足を補い、期初の通期予想を達成するものと思われますが、利益に関しましては、利益率が比較的低い低価格ツールの占める割合が増えていることが影響し若干減少する見通しです。";
		caboList = parser.parse(StringUtilities.join("\n", ExecCabocha.exec(sentence)));
		causal = this.ce.getCausalExpression(caboList, clue, this.ce.getCoreIds(caboList, clue)[0], sentence, "");
		seikai = new Causal("下半期では、上半期に導入を予定していながら諸事情により計画が遅れた案件の成約が見込めます", "売上高に関しましては上半期の不足を補い、期初の通期予想を達成する", "", "A");
		assertThat(seikai.basis, is(causal.basis));
		assertThat(seikai.result, is(causal.result));
		assertThat(seikai.subj, is(causal.subj));
		assertThat(seikai.pattern, is(causal.pattern));
	}
	
	@Test
	public void testGetInga() throws Exception {
		ArrayList<Causal> causalList = this.ce.getInga("src/extractCausal/test00.txt");
		assertThat(causalList.size(), is(0));
		
		causalList = this.ce.getInga("src/extractCausal/test1.txt");
		assertThat(3, is(causalList.size()));
		Causal seikai = new Causal("製菓・製パン向けの販売が総じて低調に推移した", "各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。", "製菓原材料類は、", "B");
		assertThat(seikai.basis, is(causalList.get(0).basis));
		assertThat(seikai.result, is(causalList.get(0).result));	
		assertThat(seikai.subj, is(causalList.get(0).subj));
		assertThat(seikai.pattern, is(causalList.get(0).pattern));
		assertThat("から、", is(causalList.get(0).clue));
		assertThat(1, is(causalList.get(0).line));
		assertThat("src/extractCausal/test1.txt", is(causalList.get(0).filePath));
		assertThat("製菓原材料類は、製菓・製パン向けの販売が総じて低調に推移したことから、各種の製菓用食材や糖置換フルーツ、栗製品やその他の仕入商品が販売減となりました。", is(causalList.get(0).sentence));
	}

}

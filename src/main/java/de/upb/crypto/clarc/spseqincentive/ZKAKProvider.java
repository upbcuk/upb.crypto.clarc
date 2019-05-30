package de.upb.crypto.clarc.spseqincentive;

import de.upb.crypto.clarc.predicategeneration.rangeproofs.zerotoupowlrangeproof.ZeroToUPowLRangeProofProtocol;
import de.upb.crypto.clarc.predicategeneration.rangeproofs.zerotoupowlrangeproof.ZeroToUPowLRangeProofProtocolFactory;
import de.upb.crypto.clarc.predicategeneration.rangeproofs.zerotoupowlrangeproof.ZeroToUPowLRangeProofPublicParameters;
import de.upb.crypto.clarc.protocols.arguments.SigmaProtocol;
import de.upb.crypto.clarc.protocols.expressions.arith.*;
import de.upb.crypto.clarc.protocols.expressions.comparison.ArithComparisonExpression;
import de.upb.crypto.clarc.protocols.expressions.comparison.GroupElementEqualityExpression;
import de.upb.crypto.clarc.protocols.generalizedschnorrprotocol.GeneralizedSchnorrProtocol;
import de.upb.crypto.clarc.protocols.protocolfactory.GeneralizedSchnorrProtocolFactory;
import de.upb.crypto.craco.commitment.pedersen.PedersenCommitmentPair;
import de.upb.crypto.craco.commitment.pedersen.PedersenCommitmentValue;
import de.upb.crypto.craco.common.GroupElementPlainText;
import de.upb.crypto.craco.common.MessageBlock;
import de.upb.crypto.craco.enc.asym.elgamal.ElgamalCipherText;
import de.upb.crypto.craco.sig.ps.PSExtendedVerificationKey;
import de.upb.crypto.craco.sig.ps.PSSignature;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignature;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;
import de.upb.crypto.math.structures.zn.Zp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ZKAKProvider {

	/* Defines the factory for the ZKAK ran in the Issue/Receive protocol */
	private static GeneralizedSchnorrProtocolFactory getIssueJoinProtocolFactory(IncentiveSystemPublicParameters pp, Zp zp, IncentiveUserPublicKey userPublicKey, IncentiveProviderPublicKey pk, MessageBlock cPre, GroupElement bCom) {

		ZnVariable uskVar = new ZnVariable("usk");
		ZnVariable uskuVar = new ZnVariable("usku");
		ZnVariable eskusrVar = new ZnVariable("eskusr");
		ZnVariable dsrnd0Var = new ZnVariable("dsrnd0");
		ZnVariable dsrnd1Var = new ZnVariable("dsrnd1");
		ZnVariable zVar = new ZnVariable("z");
		ZnVariable tVar = new ZnVariable("t");
		ZnVariable uVar = new ZnVariable("u");
		ZnVariable uInvVar = new ZnVariable("uinv");
		ZnVariable openVar = new ZnVariable("open");
		ZnVariable openUVar = new ZnVariable("openu");


		// problem 1: Cpre =
		ArithGroupElementExpression h1Expr = new NumberGroupElementLiteral(pk.h1to6[0]);
		ArithGroupElementExpression h2Expr = new NumberGroupElementLiteral(pk.h1to6[1]);
		ArithGroupElementExpression h3Expr = new NumberGroupElementLiteral(pk.h1to6[2]);
		ArithGroupElementExpression h4Expr = new NumberGroupElementLiteral(pk.h1to6[3]);
		ArithGroupElementExpression h6Expr = new NumberGroupElementLiteral(pk.h1to6[5]);
		ArithGroupElementExpression h7Expr = new NumberGroupElementLiteral(pp.h7);

		ArithGroupElementExpression h1UskUExpr = new PowerGroupElementExpression(h1Expr, uskuVar);
		ArithGroupElementExpression h2EskusrExpr = new PowerGroupElementExpression(h2Expr, eskusrVar);
		ArithGroupElementExpression h3Dsrnd0Expr = new PowerGroupElementExpression(h3Expr, dsrnd0Var);
		ArithGroupElementExpression h4Dsrnd1Expr = new PowerGroupElementExpression(h4Expr, dsrnd1Var);
		ArithGroupElementExpression h6ZExpr = new PowerGroupElementExpression(h6Expr, zVar);
		ArithGroupElementExpression h7TExpr = new PowerGroupElementExpression(h7Expr, tVar);

		NumberGroupElementLiteral cPre0 = new NumberGroupElementLiteral(((GroupElementPlainText)cPre.get(0)).get());

		ArithGroupElementExpression rhsExpr = new ProductGroupElementExpression(h1UskUExpr, h2EskusrExpr,h3Dsrnd0Expr,h4Dsrnd1Expr,h6ZExpr,h7TExpr);
		GroupElementEqualityExpression problem1 = new GroupElementEqualityExpression(cPre0, rhsExpr);

		// problem 2: upk = w^{usk}
		ArithGroupElementExpression upkExpr = new NumberGroupElementLiteral(userPublicKey.upk);
		NumberGroupElementLiteral wExpr = new NumberGroupElementLiteral(pp.w);
		PowerGroupElementExpression wUskExpr = new PowerGroupElementExpression(wExpr, uskVar);

		GroupElementEqualityExpression problem2 = new GroupElementEqualityExpression(upkExpr, new ProductGroupElementExpression(wUskExpr));

		// problem 1b: g1^u
		NumberGroupElementLiteral cPre1 = new NumberGroupElementLiteral(((GroupElementPlainText)cPre.get(1)).get());
		ArithGroupElementExpression g1Expr = new NumberGroupElementLiteral(pp.g1);

		PowerGroupElementExpression g1UExpr = new PowerGroupElementExpression(g1Expr, uVar);
		GroupElementEqualityExpression problem1b = new GroupElementEqualityExpression(cPre1, new ProductGroupElementExpression(g1UExpr));


		// problem 3: bCom = h1^usk * g1^open
		NumberGroupElementLiteral bComLit = new NumberGroupElementLiteral(bCom);
		ArithGroupElementExpression h1UskExpr = new PowerGroupElementExpression(h1Expr, uskVar);
		ArithGroupElementExpression g1OpenExpr = new PowerGroupElementExpression(g1Expr, openVar);
		ArithGroupElementExpression rhsBComExpr = new ProductGroupElementExpression(h1UskExpr,g1OpenExpr);
		GroupElementEqualityExpression problem3 = new GroupElementEqualityExpression(bComLit, rhsBComExpr);


		// problem 4: bCom^u = h1^{usk*u} * g1^{open*u}
		NumberGroupElementLiteral neutralLit = new NumberGroupElementLiteral(pp.group.getG1().getNeutralElement());
		NumberGroupElementLiteral bComInvLit = new NumberGroupElementLiteral(bCom.inv());
		PowerGroupElementExpression bComUExpr = new PowerGroupElementExpression(bComInvLit, uVar);
		ArithGroupElementExpression g1OpenUExpr = new PowerGroupElementExpression(g1Expr, openUVar);
		ArithGroupElementExpression rhsBComUExpr = new ProductGroupElementExpression(h1UskUExpr,g1OpenUExpr,bComUExpr);
		GroupElementEqualityExpression problem4 = new GroupElementEqualityExpression(neutralLit, rhsBComUExpr);


		return new GeneralizedSchnorrProtocolFactory(new GroupElementEqualityExpression[]{problem1, problem1b, problem2, problem3, problem4}, zp);
	}

	private static GeneralizedSchnorrProtocolFactory getSpendPhase1ProtocolFactory(IncentiveSystemPublicParameters pp, Zp zp, IncentiveUserPublicKey userPublicKey, IncentiveProviderPublicKey pk, MessageBlock cPre, GroupElement bCom) {
		ZnVariable uskVar = new ZnVariable("usk");
		ZnVariable uskuVar = new ZnVariable("usku");
		ZnVariable eskusrVar = new ZnVariable("eskusr");
		ZnVariable dsrnd0Var = new ZnVariable("dsrnd0");
		ZnVariable dsrnd1Var = new ZnVariable("dsrnd1");
		ZnVariable zVar = new ZnVariable("z");
		ZnVariable tVar = new ZnVariable("t");
		ZnVariable uVar = new ZnVariable("u");
		ZnVariable uInvVar = new ZnVariable("uinv");
		ZnVariable openVar = new ZnVariable("open");
		ZnVariable openUVar = new ZnVariable("openu");


		// problem 1: Cpre =
		ArithGroupElementExpression h1Expr = new NumberGroupElementLiteral(pk.h1to6[0]);
		ArithGroupElementExpression h2Expr = new NumberGroupElementLiteral(pk.h1to6[1]);
		ArithGroupElementExpression h3Expr = new NumberGroupElementLiteral(pk.h1to6[2]);
		ArithGroupElementExpression h4Expr = new NumberGroupElementLiteral(pk.h1to6[3]);
		ArithGroupElementExpression h6Expr = new NumberGroupElementLiteral(pk.h1to6[5]);
		ArithGroupElementExpression h7Expr = new NumberGroupElementLiteral(pp.h7);

		ArithGroupElementExpression h1UskUExpr = new PowerGroupElementExpression(h1Expr, uskuVar);
		ArithGroupElementExpression h2EskusrExpr = new PowerGroupElementExpression(h2Expr, eskusrVar);
		ArithGroupElementExpression h3Dsrnd0Expr = new PowerGroupElementExpression(h3Expr, dsrnd0Var);
		ArithGroupElementExpression h4Dsrnd1Expr = new PowerGroupElementExpression(h4Expr, dsrnd1Var);
		ArithGroupElementExpression h6ZExpr = new PowerGroupElementExpression(h6Expr, zVar);
		ArithGroupElementExpression h7TExpr = new PowerGroupElementExpression(h7Expr, tVar);

		NumberGroupElementLiteral cPre0 = new NumberGroupElementLiteral(((GroupElementPlainText)cPre.get(0)).get());

		ArithGroupElementExpression rhsExpr = new ProductGroupElementExpression(h1UskUExpr, h2EskusrExpr,h3Dsrnd0Expr,h4Dsrnd1Expr,h6ZExpr,h7TExpr);
		GroupElementEqualityExpression problem1 = new GroupElementEqualityExpression(cPre0, rhsExpr);


		// problem 1b: g1^u
		NumberGroupElementLiteral cPre1 = new NumberGroupElementLiteral(((GroupElementPlainText)cPre.get(1)).get());
		ArithGroupElementExpression g1Expr = new NumberGroupElementLiteral(pp.g1);

		PowerGroupElementExpression g1UExpr = new PowerGroupElementExpression(g1Expr, uVar);
		GroupElementEqualityExpression problem1b = new GroupElementEqualityExpression(cPre1, new ProductGroupElementExpression(g1UExpr));


		// problem 3: bCom = h1^usk * g1^open
		NumberGroupElementLiteral bComLit = new NumberGroupElementLiteral(bCom);
		ArithGroupElementExpression h1UskExpr = new PowerGroupElementExpression(h1Expr, uskVar);
		ArithGroupElementExpression g1OpenExpr = new PowerGroupElementExpression(g1Expr, openVar);
		ArithGroupElementExpression rhsBComExpr = new ProductGroupElementExpression(h1UskExpr,g1OpenExpr);
		GroupElementEqualityExpression problem3 = new GroupElementEqualityExpression(bComLit, rhsBComExpr);


		// problem 4: bCom^u = h1^{usk*u} * g1^{open*u}
		NumberGroupElementLiteral neutralLit = new NumberGroupElementLiteral(pp.group.getG1().getNeutralElement());
		NumberGroupElementLiteral bComInvLit = new NumberGroupElementLiteral(bCom.inv());
		PowerGroupElementExpression bComUExpr = new PowerGroupElementExpression(bComInvLit, uVar);
		ArithGroupElementExpression g1OpenUExpr = new PowerGroupElementExpression(g1Expr, openUVar);
		ArithGroupElementExpression rhsBComUExpr = new ProductGroupElementExpression(h1UskUExpr,g1OpenUExpr,bComUExpr);
		GroupElementEqualityExpression problem4 = new GroupElementEqualityExpression(neutralLit, rhsBComUExpr);


		return new GeneralizedSchnorrProtocolFactory(new GroupElementEqualityExpression[]{problem1, problem1b, problem3, problem4}, zp);
	}


	/* Returns the prover protocol of the ZKAK ran in Issue/Receive */
	static SigmaProtocol getIssueReceiveProverProtocol(IncentiveSystemPublicParameters pp, Zp zp, CPreComProofInstance joinInstance) {

		HashMap<String, Zp.ZpElement> witnessMapping = new HashMap<>();
		witnessMapping.put("usk", joinInstance.usrKeypair.userSecretKey.usk);
		witnessMapping.put("usku", joinInstance.usrKeypair.userSecretKey.usk.mul(joinInstance.u));
		witnessMapping.put("eskusr", joinInstance.eskusr.mul(joinInstance.u));
		witnessMapping.put("dsrnd0", joinInstance.dsrnd0.mul(joinInstance.u));
		witnessMapping.put("dsrnd1", joinInstance.dsrnd1.mul(joinInstance.u));
		witnessMapping.put("z", joinInstance.z.mul(joinInstance.u));
		witnessMapping.put("t", joinInstance.t.mul(joinInstance.u));
		witnessMapping.put("u", joinInstance.u);
		witnessMapping.put("uinv", joinInstance.u.inv());
		witnessMapping.put("open", joinInstance.open);
		witnessMapping.put("openu", joinInstance.open.mul(joinInstance.u));


		return getIssueJoinProtocolFactory(pp, zp, joinInstance.usrKeypair.userPublicKey, joinInstance.pk, joinInstance.cPre, joinInstance.bCom).createProverGeneralizedSchnorrProtocol(witnessMapping);
	}


	public static SigmaProtocol getSpendPhase1ProverProtocol(IncentiveSystemPublicParameters pp, Zp zp, SpendPhase1Instance spendPhase1Instance) {
		HashMap<String, Zp.ZpElement> witnessMapping = new HashMap<>();
		witnessMapping.put("usk", spendPhase1Instance.usrKeypair.userSecretKey.usk);
		witnessMapping.put("usku", spendPhase1Instance.usrKeypair.userSecretKey.usk.mul(spendPhase1Instance.u));
		witnessMapping.put("eskusr", spendPhase1Instance.eskusr.mul(spendPhase1Instance.u));
		witnessMapping.put("dsrnd0", spendPhase1Instance.dsrnd0.mul(spendPhase1Instance.u));
		witnessMapping.put("dsrnd1", spendPhase1Instance.dsrnd1.mul(spendPhase1Instance.u));
		witnessMapping.put("z", spendPhase1Instance.z.mul(spendPhase1Instance.u));
		witnessMapping.put("t", spendPhase1Instance.t.mul(spendPhase1Instance.u));
		witnessMapping.put("u", spendPhase1Instance.u);
		witnessMapping.put("uinv", spendPhase1Instance.u.inv());
		witnessMapping.put("open", spendPhase1Instance.open);
		witnessMapping.put("openu", spendPhase1Instance.open.mul(spendPhase1Instance.u));


		return getSpendPhase1ProtocolFactory(pp, zp, spendPhase1Instance.usrKeypair.userPublicKey, spendPhase1Instance.pk, spendPhase1Instance.cPre, spendPhase1Instance.bCom).createProverGeneralizedSchnorrProtocol(witnessMapping);

	}


	static SigmaProtocol getSpendPhase1VerifierProtocol(IncentiveSystemPublicParameters pp, Zp zp, IncentiveUserPublicKey userPublicKey, IncentiveProviderPublicKey providerPublicKey, MessageBlock cPre, GroupElement bCom) {
		return getSpendPhase1ProtocolFactory(pp, zp, userPublicKey, providerPublicKey, cPre, bCom).createVerifierGeneralizedSchnorrProtocol();
	}

	static SigmaProtocol getIssueJoinVerifierProtocol(IncentiveSystemPublicParameters pp, Zp zp, IncentiveUserPublicKey userPublicKey, IncentiveProviderPublicKey providerPublicKey, MessageBlock cPre, GroupElement bCom) {
		return getIssueJoinProtocolFactory(pp, zp, userPublicKey, providerPublicKey, cPre, bCom).createVerifierGeneralizedSchnorrProtocol();
	}

	private static GeneralizedSchnorrProtocolFactory getPSVerifyProtocolFactory(IncentiveSystemPublicParameters pp, SPSEQSignature spseqSignature, IncentiveProviderPublicKey pk) {
		//GroupElementEqualityExpression problem = getPSVerifyProtocolProblem(pp, spseqSignature, pk);
		return null;
	}
	/*
	private static GroupElementEqualityExpression getPSVerifyProtocolProblem(IncentiveSystemPublicParameters pp, PSSignature randtoken, PSExtendedVerificationKey pk) {
		ZnVariable uskVar = new ZnVariable("usk");
		ZnVariable dsidVar = new ZnVariable("dsid");
		ZnVariable dsrndVar = new ZnVariable("dsrnd");
		ZnVariable vVar = new ZnVariable("v");
		ZnVariable rPrimeVar = new ZnVariable("rPrime");

		BilinearMap e = pp.group.getBilinearMap();

		GroupElement sigma0 = randtoken.getGroup1ElementSigma1();
		GroupElement sigma1 = randtoken.getGroup1ElementSigma2();

		GroupElement tildeX = pk.getGroup2ElementTildeX();
		GroupElement tildeG = pk.getGroup2ElementTildeG();
		GroupElement lhs = (e.apply(sigma0, tildeX).inv()).op(e.apply(sigma1, tildeG));
		ArithGroupElementExpression lhsExpr = new NumberGroupElementLiteral(lhs);

		ArithGroupElementExpression sigma0Expr = new NumberGroupElementLiteral(sigma0);
		ArithGroupElementExpression tildeGExpr = new NumberGroupElementLiteral(tildeG);
		ArithGroupElementExpression tildeY1Expr = new NumberGroupElementLiteral(pk.getGroup2ElementsTildeYi()[0]);
		ArithGroupElementExpression tildeY2Expr = new NumberGroupElementLiteral(pk.getGroup2ElementsTildeYi()[1]);
		ArithGroupElementExpression tildeY3Expr = new NumberGroupElementLiteral(pk.getGroup2ElementsTildeYi()[2]);
		ArithGroupElementExpression tildeY4Expr = new NumberGroupElementLiteral(pk.getGroup2ElementsTildeYi()[3]);

		List<ArithGroupElementExpression> factorsRHS = Arrays.asList(
				// e( sigma0, g1~)^r'
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeGExpr), rPrimeVar),
				// e(sigma0, Y1~)^usk
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeY1Expr), uskVar),
				// e(sigma0, Y2~)^dsidInGroup
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeY2Expr), dsidVar),
				// e(sigma0, Y3~)^dsrnd
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeY3Expr), dsrndVar),
				// e(sigma0, Y4~)^v
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeY4Expr), vVar)
		);

		return new GroupElementEqualityExpression(lhsExpr, new ProductGroupElementExpression(factorsRHS));
	}
	*/

	static GeneralizedSchnorrProtocol getCreditEarnProverProtocol(IncentiveSystemPublicParameters pp, SPSEQSignature spseqSignatureR, IncentiveProviderPublicKey pk, Zp.ZpElement usk, IncentiveToken token, Zp.ZpElement s) {
		HashMap<String, Zp.ZpElement> witnessMapping = new HashMap<>();
		witnessMapping.put("usk", usk);
		witnessMapping.put("eskusr", token.esk);
		witnessMapping.put("dsrnd0", token.dsrnd0);
		witnessMapping.put("dsrnd1", token.dsrnd1);
		witnessMapping.put("z", token.z);
		witnessMapping.put("t", token.t);
		witnessMapping.put("s", s);

		return null;
	}

	static GeneralizedSchnorrProtocol getCreditEarnVerifierProtocol(IncentiveSystemPublicParameters pp, SPSEQSignature spseqSignature, IncentiveProviderPublicKey pk) {
		return getPSVerifyProtocolFactory(pp, spseqSignature, pk).createVerifierGeneralizedSchnorrProtocol();
	}

	private static GeneralizedSchnorrProtocolFactory getSpendDeductSchnoorProtocolFactory(IncentiveSystemPublicParameters pp, Zp.ZpElement dsid, Zp.ZpElement c, PSExtendedVerificationKey pk, PSSignature blindedSig, PedersenCommitmentValue commitmentOfValue, ElgamalCipherText ctrace, Zp.ZpElement k, PedersenCommitmentValue commitment, ElgamalCipherText cDsidStar, Zp.ZpElement gamma) {

		ZnVariable uskVar = new ZnVariable("usk");
		ZnVariable dsrndVar = new ZnVariable("dsrnd");
		ZnVariable vVar = new ZnVariable("v");
		ZnVariable dsidStarVar = new ZnVariable("dsidStar");
		ZnVariable dsrndStarVar = new ZnVariable("dsrndStar");
		ZnVariable rPrimeVar = new ZnVariable("rPrime");
		ZnVariable rVar = new ZnVariable("r");
		ZnVariable rCVar = new ZnVariable("rC");
		ZnVariable openStarVar = new ZnVariable("openStar");
		ZnVariable rVVar = new ZnVariable("rV");

		ArithGroupElementExpression wExpr = new NumberGroupElementLiteral(pp.w);

		// problem 1: c = usk * gamma + dsrnd <=> w^c = (w^{gamma})^usk * w^dsrnd
		NumberGroupElementLiteral lhs1 = new NumberGroupElementLiteral(pp.w.pow(c));

		PowerGroupElementExpression factor1 = new PowerGroupElementExpression(new NumberGroupElementLiteral(pp.w.pow(gamma)), uskVar);
		PowerGroupElementExpression factor2 = new PowerGroupElementExpression(wExpr, dsrndVar);

		ArithComparisonExpression problem1 = new GroupElementEqualityExpression(lhs1, new ProductGroupElementExpression(factor1, factor2));

		// problem 2: Vrfy randomized spseqSignature
		// Note that the second msg signed by the spseqSignature (dsid) is publicly known. Thus, we pull it on the LHS of the expression.

		BilinearMap e = pp.group.getBilinearMap();

		GroupElement sigma0 = blindedSig.getGroup1ElementSigma1();
		GroupElement sigma1 = blindedSig.getGroup1ElementSigma2();

		GroupElement tildeX = pk.getGroup2ElementTildeX();
		GroupElement tildeG = pk.getGroup2ElementTildeG();

		// lhs2 = e(sigma1, g1~) / [e(sigma0, X~)] (usual lhs of the expression)
		PairingProductExpression lhs2 = e.pairingProductExpression();
		lhs2.op(sigma0, tildeX).inv().op(sigma1, tildeG);
		// lhs2 *= 1 / [e(sigma0, Y2~)^dsid] (added due to the fact that dsid is common input)
		lhs2.op(e.pairingProductExpression().op(sigma0, pk.getGroup2ElementsTildeYi()[1], dsid).inv());
		ArithGroupElementExpression lhsExpr = new NumberGroupElementLiteral(lhs2.evaluate());

		ArithGroupElementExpression sigma0Expr = new NumberGroupElementLiteral(sigma0);
		ArithGroupElementExpression tildeGExpr = new NumberGroupElementLiteral(tildeG);
		ArithGroupElementExpression tildeY1Expr = new NumberGroupElementLiteral(pk.getGroup2ElementsTildeYi()[0]);
		ArithGroupElementExpression tildeY3Expr = new NumberGroupElementLiteral(pk.getGroup2ElementsTildeYi()[2]);
		ArithGroupElementExpression tildeY4Expr = new NumberGroupElementLiteral(pk.getGroup2ElementsTildeYi()[3]);

		List<ArithGroupElementExpression> factorsRHS = Arrays.asList(
				// e( sigma0, g1~)^r'
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeGExpr), rPrimeVar),
				// e(sigma0, Y1~)^usk
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeY1Expr), uskVar),
				// e(sigma0, Y3~)^dsrnd
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeY3Expr), dsrndVar),
				// e(sigma0, Y4~)^v
				new PowerGroupElementExpression(new PairingGroupElementExpression(e, sigma0Expr, tildeY4Expr), vVar)
		);

		GroupElementEqualityExpression problem2 = new GroupElementEqualityExpression(lhsExpr, new ProductGroupElementExpression(factorsRHS));

		// problem 4: ctrace = (c1, c2) :: (a) c1 = w^r AND (b) c2 = (c1)^usk * w^{dsid*}
		ArithGroupElementExpression c1Expr = new NumberGroupElementLiteral(ctrace.getC1());
		ArithGroupElementExpression c2Expr = new NumberGroupElementLiteral(ctrace.getC2());

		ArithComparisonExpression problem4a = getElgamalProblemA(ctrace.getC1(), wExpr, rVar);
		ArithComparisonExpression problem4b = getElgamalProblemB(ctrace.getC2(), c1Expr, uskVar, wExpr, dsidStarVar);

		// problem 5: {g1^{y_1}}^usk {g1^{y_2}}^dsid* {g1^{y_3}}^dsrnd* {g_{y_3}}^{v-k}  g1^rC
		ArithGroupElementExpression gY1Expr = new NumberGroupElementLiteral(pk.getGroup1ElementsYi()[0]);
		ArithGroupElementExpression gY2Expr = new NumberGroupElementLiteral(pk.getGroup1ElementsYi()[1]);
		ArithGroupElementExpression gY3Expr = new NumberGroupElementLiteral(pk.getGroup1ElementsYi()[2]);
		ArithGroupElementExpression gY4Expr = new NumberGroupElementLiteral(pk.getGroup1ElementsYi()[3]);
		ArithGroupElementExpression gExpr = new NumberGroupElementLiteral(pk.getGroup1ElementG());

		// C * (g1^{y4})^k (due to the fact that k is public)
		PowProductExpression lhs5 = commitment.getCommitmentElement()
										.asPowProductExpression()
										.op(pk.getGroup1ElementsYi()[3], k);
		NumberGroupElementLiteral problem5Lhs = new NumberGroupElementLiteral(lhs5.evaluate());

		List<ArithGroupElementExpression> problem5RhsFactors = Arrays.asList(
				new PowerGroupElementExpression(gY1Expr, uskVar),
				new PowerGroupElementExpression(gY2Expr, dsidStarVar),
				new PowerGroupElementExpression(gY3Expr, dsrndStarVar),
				new PowerGroupElementExpression(gY4Expr, vVar),
				new PowerGroupElementExpression(gExpr, rCVar)
		);
		ArithComparisonExpression problem5 = new GroupElementEqualityExpression(problem5Lhs, new ProductGroupElementExpression(problem5RhsFactors));

		// problem 6: C_dsid* = (c1, c2) = (g1^(open*), g1^dsid* h7^(open*)) :: (a) c1 = g1^(open*) AND (b) c2 = g1^dsid* h7^(open*)
		ArithGroupElementExpression gPPExpr = new NumberGroupElementLiteral(pp.g1);
		ArithGroupElementExpression hExpr = new NumberGroupElementLiteral(pp.h7);

		// 1st component
		GroupElementEqualityExpression problem6a = getElgamalProblemA(cDsidStar.getC1(), gPPExpr, openStarVar);
		// 2nd component
		GroupElementEqualityExpression problem6b = getElgamalProblemB(cDsidStar.getC2(), gPPExpr, dsidStarVar, hExpr, openStarVar);

		// problem 3: commitment on value is valid for v, needed for the range proof.
		// cV = (h7)^v * g1^{rV}
		ArithGroupElementExpression cVExpr = new NumberGroupElementLiteral(commitmentOfValue.getCommitmentElement());
		ProductGroupElementExpression rhs = new ProductGroupElementExpression(new PowerGroupElementExpression(hExpr, vVar), new PowerGroupElementExpression(gPPExpr, rVVar));
		GroupElementEqualityExpression problem3 = new GroupElementEqualityExpression(cVExpr, rhs);

		return new GeneralizedSchnorrProtocolFactory(
				new ArithComparisonExpression[]{problem1, problem2, problem3, problem4a, problem4b, problem5, problem6a, problem6b},
				new Zp(pp.group.getG1().size())
		);
	}

	static GeneralizedSchnorrProtocol getSpendDeductSchnorrProverProtocol(IncentiveSystemPublicParameters pp, Zp.ZpElement c, Zp.ZpElement gamma, PSExtendedVerificationKey pk, PSSignature blindedSig, Zp.ZpElement k, ElgamalCipherText ctrace, PedersenCommitmentValue commitment, PedersenCommitmentPair commitmentOfValue, Zp.ZpElement usk, Zp.ZpElement dsid, Zp.ZpElement dsrnd, Zp.ZpElement dsidStar, Zp.ZpElement dsrndStar, Zp.ZpElement r, Zp.ZpElement rC, Zp.ZpElement rPrime, Zp.ZpElement v, Zp.ZpElement openStar, ElgamalCipherText cDsidStar) {

		GeneralizedSchnorrProtocolFactory schnorrFac = getSpendDeductSchnoorProtocolFactory(pp, dsid, c, pk, blindedSig, commitmentOfValue.getCommitmentValue(), ctrace, k, commitment, cDsidStar, gamma);

		HashMap<String, Zp.ZpElement> witnessMapping = new HashMap<>();
		witnessMapping.put("usk", usk);
		witnessMapping.put("dsid", dsid);
		witnessMapping.put("dsrnd", dsrnd);
		witnessMapping.put("v", v);
		witnessMapping.put("dsidStar", dsidStar);
		witnessMapping.put("dsrndStar", dsrndStar);
		witnessMapping.put("rPrime", rPrime);
		witnessMapping.put("r", r);
		witnessMapping.put("rC", rC);
		witnessMapping.put("openStar", openStar);
		witnessMapping.put("rV", commitmentOfValue.getOpenValue().getRandomValue());

		return schnorrFac.createProverGeneralizedSchnorrProtocol(witnessMapping);
	}

	static GeneralizedSchnorrProtocol getSpendDeductSchnorrVerifierProtocol(IncentiveSystemPublicParameters pp, Zp.ZpElement c, Zp.ZpElement gamma, PSExtendedVerificationKey pk, PSSignature blindedSig, Zp.ZpElement k, ElgamalCipherText ctrace, PedersenCommitmentValue commitment, PedersenCommitmentValue commitmentOnV, Zp.ZpElement dsid, ElgamalCipherText cDsidStar) {
		GeneralizedSchnorrProtocolFactory schnorrFac = getSpendDeductSchnoorProtocolFactory(pp, dsid, c, pk, blindedSig, commitmentOnV, ctrace, k, commitment, cDsidStar, gamma);
		return schnorrFac.createVerifierGeneralizedSchnorrProtocol();
	}


	/* Note that prover and verifier need to ne generated from the **same** factory. Therefore, this factory should only be set up by either the prover or the
	 * verifier, and the parameters generated in that way should be sent to the other party. In this application, the user/prover generates the protocol and sends
     * the parameters to the verifier
	 */
	static ZeroToUPowLRangeProofProtocolFactory getSpendDeductRangeProofProtocolFactory(IncentiveSystemPublicParameters pp, PedersenCommitmentValue commitment) {
		ZeroToUPowLRangeProofPublicParameters rangePP = pp.getSpendDeductRangePP(commitment.getCommitmentElement());
		return new ZeroToUPowLRangeProofProtocolFactory(rangePP, "Spend/Deduct");
	}

	static ZeroToUPowLRangeProofProtocol getSpendDeductRangeProverProtocol(IncentiveSystemPublicParameters pp, PedersenCommitmentValue commitment, Zp.ZpElement commitmentRandomness, Zp.ZpElement committedValue) {
		ZeroToUPowLRangeProofProtocolFactory rangeFac = getSpendDeductRangeProofProtocolFactory(pp, commitment);

		return rangeFac.getProverProtocol(commitmentRandomness, committedValue);
	}
	static ZeroToUPowLRangeProofProtocol getSpendDeductRangeVerifierProtocol(IncentiveSystemPublicParameters pp, PedersenCommitmentValue commitment) {
		ZeroToUPowLRangeProofProtocolFactory rangeFac = getSpendDeductRangeProofProtocolFactory(pp, commitment);
		return rangeFac.getVerifierProtocol();
	}

	/** Problem for expression c1 = baseExpr^expVar */
	private static GroupElementEqualityExpression getElgamalProblemA(GroupElement c1, ArithGroupElementExpression baseExpr, ZnVariable expVar) {
		return new GroupElementEqualityExpression(
				new NumberGroupElementLiteral(c1),
				new ProductGroupElementExpression(new PowerGroupElementExpression(baseExpr, expVar))
		);
	}

	/** Problem for expression c2 = gExpr^aVar * hExpr^bVar */
	private static GroupElementEqualityExpression getElgamalProblemB(GroupElement c2, ArithGroupElementExpression gExpr, ZnVariable aVar, ArithGroupElementExpression hExpr, ZnVariable bVar) {
		ProductGroupElementExpression rhs3bExpr = new ProductGroupElementExpression(
				new PowerGroupElementExpression(gExpr, aVar),
				new PowerGroupElementExpression(hExpr, bVar)
		);
		return new GroupElementEqualityExpression(new NumberGroupElementLiteral(c2), rhs3bExpr);
	}



	/* Prover and Verifier protocols using PoPK */

/*	static SigmaProtocol getSpendDeductProverProtocol(IncentiveSystemPublicParameters pp, Zp.ZpElement c, Zp.ZpElement gamma, PSExtendedVerificationKey pk, PSSignature blindedSig, Zp.ZpElement k, ElgamalCipherText ctrace, PedersenCommitmentValue commitment, PedersenCommitmentPair commitmentOfValue, Zp.ZpElement usk, Zp.ZpElement dldsid, Zp.ZpElement dsrnd, Zp.ZpElement dldsidStar, Zp.ZpElement dsrndStar, Zp.ZpElement r, Zp.ZpElement rC, Zp.ZpElement rPrime, Zp.ZpElement v, PedersenCommitmentValue rangeCommitment, Zp.ZpElement rangeRandomness) {
		GeneralizedSchnorrProtocol schnorr = getSpendDeductSchnorrProverProtocol(pp, c, gamma, pk, blindedSig, k, ctrace, commitment, commitmentOfValue, usk, dldsid, dsrnd, dldsidStar, dsrndStar, r, rC, rPrime, v, , );
		SigmaProtocolPolicyFact schnorrPolicyFact = new SigmaProtocolPolicyFact(schnorr, 1);

		ZeroToUPowLRangeProofProtocol rangeProof = getSpendDeductRangeProverProtocol(pp, pk, rangeCommitment, rangeRandomness, (Zp.ZpElement) v.sub(k));
		SigmaProtocolPolicyFact rangePolicyFact = new SigmaProtocolPolicyFact(rangeProof, 2);

		ThresholdPolicy policy = new ThresholdPolicy(2, schnorrPolicyFact, rangePolicyFact);

		return new ProofOfPartialKnowledgeProtocol(
				new ProofOfPartialKnowledgePublicParameters(
						new ShamirSecretSharingSchemeProvider(),
						new Zp(pp.group.getG1().size())
				), policy
		);
	}

	static SigmaProtocol getSpendDeductVerifierProtocol(IncentiveSystemPublicParameters pp, Zp.ZpElement c, Zp.ZpElement gamma, PSExtendedVerificationKey pk, PSSignature blindedSig, Zp.ZpElement k, ElgamalCipherText ctrace, PedersenCommitmentValue commitment, PedersenCommitmentValue commitmentOnV, ArbitraryRangeProofPublicParameters rangePP) {
		GeneralizedSchnorrProtocol schnorr = getSpendDeductSchnorrVerifierProtocol(pp, c, gamma, pk, blindedSig, k, ctrace, commitment, commitmentOnV);
		SigmaProtocolPolicyFact schnorrPolicyFact = new SigmaProtocolPolicyFact(schnorr, 1);

		ZeroToUPowLRangeProofProtocol rangeProof = getSpendDeductRangeVerifierProtocol(rangePP);
		SigmaProtocolPolicyFact rangePolicyFact = new SigmaProtocolPolicyFact(rangeProof, 2);

		ThresholdPolicy policy = new ThresholdPolicy(2, schnorrPolicyFact, rangePolicyFact);

		return new ProofOfPartialKnowledgeProtocol(
				new ProofOfPartialKnowledgePublicParameters(
						new ShamirSecretSharingSchemeProvider(),
						new Zp(pp.group.getG1().size())
				), policy
		);
	}*/
}
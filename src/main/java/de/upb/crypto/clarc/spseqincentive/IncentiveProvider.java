package de.upb.crypto.clarc.spseqincentive;

import de.upb.crypto.clarc.protocols.parameters.Announcement;
import de.upb.crypto.craco.common.MessageBlock;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignature;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zp;

/**
 * Represents the provider of the incentive system,
 * <p>
 * The main task is to set up the verifier instances of the protocols ran in the system.
 */
public class IncentiveProvider {
	IncentiveSystemPublicParameters pp;
	IncentiveProviderKeyPair keyPair;

	public IncentiveProvider(IncentiveSystemPublicParameters pp) {
		this.pp = pp;
		IncentiveProviderSetup providerSetup = new IncentiveProviderSetup();
		this.keyPair = providerSetup.generateProviderKeys(pp);
	}

	// in the protocol the issuer first proofs knowledge of its secret key, here we first let the user send the commitment
	public IssueInstance initIssue(IncentiveUserPublicKey userPublicKey, MessageBlock cPre) {
		Group g1 = pp.group.getG1();
		Zp zp = new Zp(g1.size());

		// esk*_{isr} <- Zp
		Zp.ZpElement eskIsr = zp.getUniformlyRandomElement();


		return new IssueInstance(pp, keyPair, userPublicKey, eskIsr, cPre);
	}

	public CreditInstance initCredit(Zp.ZpElement k, MessageBlock cPre, SPSEQSignature spseqSignature, Announcement[] announcements) {
		return new CreditInstance(pp, keyPair.providerPublicKey, keyPair.providerSecretKey, k, cPre, spseqSignature, null, announcements);
	}


	public DeductPhase1nstance initDeductPhase1(IncentiveUserPublicKey userPublicKey, Zp.ZpElement k, GroupElement dsid, MessageBlock cPre){
		Group g1 = pp.group.getG1();
		Zp zp = new Zp(g1.size());

		// esk*_{isr} <- Zp
		Zp.ZpElement eskIsr = zp.getUniformlyRandomElement();
		// gamma <- Zp
		Zp.ZpElement gamma = zp.getUniformlyRandomElement();

		return new DeductPhase1nstance(pp, keyPair, userPublicKey, eskIsr, gamma, k, dsid, cPre);
	}
/*
	public DeductInstance initDeduct(Zp.ZpElement k, Zp.ZpElement dsid, ElgamalCipherText cUsrStar) {
		Zp zp = new Zp(pp.group.getG1().size());

		Zp.ZpElement dsidIsrStar = zp.getUniformlyRandomElement();
		Zp.ZpElement gamma = zp.getUniformlyRandomElement();

		ElgamalCipherText cDsidStar = new ElgamalCipherText(cUsrStar.getC1(), cUsrStar.getC2().op(pp.g1.pow(dsidIsrStar)));

		return new DeductInstance(pp, keyPair.providerPublicKey, keyPair.providerSecretKey, k, dsid, dsidIsrStar, gamma, cDsidStar);

	}*/

}

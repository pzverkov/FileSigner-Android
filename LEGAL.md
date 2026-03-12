# Legal Notices

## Copyright

Copyright 2026 pzverkov. All rights reserved.

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full text.

## Not a Certified Security Product

This application provides cryptographic file signing using Android KeyStore. It is **not** a certified, validated, or approved security product under any government or industry certification scheme. Specifically:

- **Not FIPS validated.** This software has not undergone FIPS 140-2 or FIPS 140-3 validation. It uses NIST-approved algorithms (ECDSA P-256, SHA-256) but the implementation itself is not certified.
- **Not a qualified electronic signature tool.** This software does not produce qualified electronic signatures as defined by eIDAS Regulation (EU) 910/2014. It does not integrate with any Qualified Trust Service Provider (QTSP) and does not produce CAdES, XAdES, or PAdES signature formats.
- **Not Common Criteria evaluated.** This software has not been evaluated under Common Criteria (ISO/IEC 15408).

The compliance alignment statements in this repository (BSI TR-02102-1, OWASP MASVS, NIST SP 800-186, FIPS 186-4) describe algorithm selection and architectural patterns. They do not constitute formal certification or endorsement by any standards body.

## Export Controls

This software implements cryptographic functionality. Cryptographic software may be subject to export controls and import restrictions depending on your jurisdiction.

- **EU:** Dual-Use Regulation (EU 2021/821) applies. Open-source software with publicly available source code generally benefits from exemptions under Article 2, but users should verify applicability.
- **US:** Export Administration Regulations (EAR) classify cryptographic software under ECCN 5D002. Publicly available open-source software may qualify for License Exception TSR (15 CFR 740.13(e)).
- **Wassenaar Arrangement:** ECDSA is listed as a controlled dual-use technology. Open-source publication generally satisfies the "publicly available" exemption.
- **Other jurisdictions:** Users are solely responsible for compliance with local import, export, and use regulations for cryptographic software. Some jurisdictions (including but not limited to China, Russia, India, UAE, Saudi Arabia, Belarus, Iran, North Korea, Cuba) impose restrictions on the import, use, or deployment of cryptographic software.

The authors and copyright holders make no representations regarding the legality of using this software in any particular jurisdiction. **Users assume full responsibility for compliance with all applicable export control and import laws.**

## EU Cyber Resilience Act (CRA)

The EU Cyber Resilience Act (Regulation 2024/2847) imposes security obligations on products with digital elements. This software is provided as non-commercial open-source under the Apache 2.0 license. Under Recital 18 of the CRA, open-source software developed or supplied outside the course of a commercial activity is not considered a "product with digital elements" and is exempt from CRA obligations.

If this software is incorporated into a commercial product, **the commercial integrator** assumes responsibility for CRA compliance, including vulnerability handling, security updates, and conformity assessment procedures.

## eIDAS Regulation

This software produces raw ECDSA digital signatures in DER format. These are **not** qualified electronic signatures (QES) or advanced electronic signatures (AdES) as defined by:

- eIDAS Regulation (EU) 910/2014, Articles 3(10-12), 26, 28
- ETSI EN 319 122 (CAdES), ETSI EN 319 132 (XAdES), ETSI EN 319 142 (PAdES)

For signatures with legal standing equivalent to handwritten signatures under EU law, signatures must be created using a Qualified Electronic Signature Creation Device (QSCD) and issued through a Qualified Trust Service Provider (QTSP) listed on the EU Trusted List.

## Jurisdiction and Governing Law

This software is developed and maintained from Spain (EU). Any disputes arising from the use of this software shall be governed by the laws of Spain, without regard to conflict of law provisions, and subject to the exclusive jurisdiction of the courts of Spain, unless otherwise required by mandatory consumer protection laws of the user's jurisdiction.

## Limitation of Liability

TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, IN NO EVENT SHALL THE AUTHORS, COPYRIGHT HOLDERS, OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

This limitation applies regardless of whether the damages arise from use or misuse of the software, inability to use the software, unauthorized modification or access, failure of the software to perform as expected, or any other cause.

## No Legal Advice

Nothing in this repository  - including documentation, compliance matrices, security design documents, and this legal notice  - constitutes legal, regulatory, or compliance advice. Users should consult qualified legal counsel for guidance on applicable laws and regulations in their jurisdiction.

## User Responsibility

By using, cloning, forking, or incorporating this software, you acknowledge and agree that:

1. You are solely responsible for determining the legality of using this software in your jurisdiction.
2. You are solely responsible for compliance with all applicable laws, regulations, and standards.
3. You will not use this software to circumvent any applicable laws or regulations.
4. You will not use this software in any manner that violates the rights of any third party.
5. The authors and copyright holders bear no responsibility for how this software is used or deployed.

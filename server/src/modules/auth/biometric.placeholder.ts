// Biometric placeholder: actual biometric auth is handled client-side (Android)
// Server may accept a signed assertion later for advanced flows.
export function verifyBiometricAssertion(_assertion: string): boolean {
  // TODO: implement challenge-response if required
  return true;
}


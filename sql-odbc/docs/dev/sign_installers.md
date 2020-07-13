# Signing Installers

## Steps to sign odbc driver windows installer 

- Get code signing certificate. Certificate must meet some [criteria](https://docs.microsoft.com/en-us/windows/win32/appxpkg/how-to-sign-a-package-using-signtool). Some options are listed below.
  - Buy [Extended Validation (EV) Code Signing Certificate](https://docs.microsoft.com/en-us/windows-hardware/drivers/dashboard/get-a-code-signing-certificate#step-2-buy-a-new-code-signing-certificate)
  - Create a [self-signed certificate](https://docs.microsoft.com/en-us/windows/msix/package/create-certificate-package-signing#create-a-self-signed-certificate) (For testing purpose only).
- Install the certificate that you want to sign the file with.
  - [Import-Certificate](https://docs.microsoft.com/en-us/powershell/module/pkiclient/import-certificate?view=win10-ps) can be used for this purpose
 
```
// Import certificate as Trusted Publisher
Import-Certificate -FilePath .\code_signing.crt -Cert Cert:\CurrentUser\TrustedPublisher

// Import certificate as a Root certificate authority.
Import-Certificate -FilePath .\code_signing.crt -Cert Cert:\CurrentUser\Root
```

- Sign the .msi file. 
  - Sign installer using [SignTool](https://docs.microsoft.com/en-us/windows/msix/package/sign-app-package-using-signtool)

```
  signtool sign /sha1 <CertificateHash> '.\Open Distro for Elasticsearch SQL ODBC Driver-<version>-Windows.msi' 
```
  
  - Alternatively, [Set-AuthenticodeSignature](https://docs.microsoft.com/en-us/powershell/module/microsoft.powershell.security/set-authenticodesignature?view=powershell-7) can be used for adding digital signature.

```
  Set-AuthenticodeSignature '.\Open Distro for Elasticsearch SQL ODBC Driver-<version>-Windows.msi' -Certificate (Get-ChildItem Cert:\CurrentUser\My -CodeSigningCert) -TimestampServer "http://timestamp.verisign.com/scripts/timstamp.dll"
```

[Note](https://stackoverflow.com/questions/50956108/codesign-software-still-gives-a-warning-on-windows-10): If you have a standard code signing certificate, some time will be needed for your application to build trust. Microsoft affirms that an Extended Validation (EV) Code Signing Certificate allows to skip this period of trust building. According to Microsoft, extended validation certificates allow the developer to immediately establish reputation with SmartScreen. Otherwise, the users will see a warning like "Windows Defender Smartscreen prevented an unrecognized app from starting. Running this app might put your PC at risk.", with the two buttons: "Run anyway" and "Don't run". 


## Steps to sign odbc driver macOS installer 

- Get a [Developer ID Installer signing certificate](https://help.apple.com/xcode/mac/current/#/dev154b28f09)
- Sign the installer package using `productsign`. Do not use `Developer ID Application certificate`.

```
productsign -sign "Developer ID Installer: Your Apple Account Name (**********)" "~/Desktop/Open Distro for Elasticsearch SQL ODBC Driver-<version>-Darwin.pkg" "~/Desktop/signed-Open Distro for Elasticsearch SQL ODBC Driver-<version>-Darwin.pkg"
```

- Test installer package using [spctl](http://www.manpagez.com/man/8/spctl/)
```
  spctl -a -v --type install "Desktop/Open Distro for Elasticsearch SQL ODBC Driver-<version>-Darwin.pkg"
```

Reference: https://help.apple.com/xcode/mac/current/#/deve51ce7c3d
;NSIS Modern User Interface
;RMK mafoclient installer
;Written by sebastian krämer

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;General

  ;Name and file
  Name "RMK MF-Assistent buildnumber"
  OutFile "rmkmfassistent.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\rmk\mfassistent"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\rmkmfassistent" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "lizenz.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "German"

;--------------------------------
;Installer Sections

Section "RMK MF-Assistent" SecDummy

  SetOutPath "$INSTDIR"
  
  ;main jar file
  File ..\finaljars\RMKMafo.jar
  ;resources but exclude svn directory 
  File /r /x .svn ..\resources
  ;sip-stuff but exclude svn directory 
  File /r /x .svn ..\sip
  ;dependency jars
  File /r /x .svn /x poi* /x jfreech* ..\jars
  
  ;Store installation folder
  WriteRegStr HKCU "Software\mfassistent" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  ; create desktop shortcut
  CreateShortCut "$DESKTOP\RMKMafo.lnk" "$INSTDIR\RMKMafo.jar" "" "$INSTDIR\resources\rmk-mafo.ico"

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecDummy ${LANG_ENGLISH} "Installiert den RMK MF-Assistent."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecDummy} $(DESC_SecDummy)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ;ADD YOUR OWN FILES HERE...
  Delete "$INSTDIR\*"
  Delete "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\mfassistent"

SectionEnd
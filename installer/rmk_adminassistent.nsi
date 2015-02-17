;NSIS Modern User Interface
;OVT adminassi installer
;Written by sebastian krämer

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;General

  ;Name and file
  Name "RMK Admin-Assistent buildnumber"
  OutFile "rmkadminassistent.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\rmk\adminassistent"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\rmkadminassistent" ""

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

Section "RMK Admin-Assistent" SecDummy

  SetOutPath "$INSTDIR"
  
  ;ADD YOUR OWN FILES HERE...
  File ..\finaljars\RMKAdmin.jar
  ;File ..\ovt.properties
  ;dependency jars
  File /r /x .svn /x poi-con* /x poi-scrat* ..\jars
  File /r /x .svn ..\resources
  File /r /x .svn ..\vorlagen
  
  ;Store installation folder
  WriteRegStr HKCU "Software\rmkadminassistent" "" $INSTDIR
  DeleteRegKey /ifempty HKCU "Software\adminassistent"
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  ; create desktop shortcut
  CreateShortCut "$DESKTOP\RMKAdmin.lnk" "$INSTDIR\RMKAdmin.jar" "" "$INSTDIR\resources\rmk-admin.ico"

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecDummy ${LANG_ENGLISH} "Installiert den RMK Admin-Assistenten."

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

  DeleteRegKey /ifempty HKCU "Software\adminassistent"
  DeleteRegKey /ifempty HKCU "Software\rmkadminassistent"

SectionEnd
import { NativeModules } from 'react-native';

const { DvRnSunmiDevices } = NativeModules;

type SunmiPrinter = {
  printCustomHTMl: (htmlToConvert: string) => Promise<boolean>;
  showTwoLineText: (firstRow: string, secondRow: string) => Promise<boolean>;
  writeNFCTag: (data: ReadonlyMap<String, String>) => Promise<boolean>;
  /**
   *  0 - UNKNOWN_STATE
   *  1 - READY_FOR_PRINT
   *  2 - PREPARING_PRINTER
   *  3 - ABNORMAL_COMMUNICATION
   *  4 - OUT_OF_PAPER
   *  5 - OVERHEATED
   *  6 - OPEN_THE_LID
   *  7 - PAPER_CUT_IS_ABNORMAL
   *  8 - PAPER_CUT_RECOVERED
   *  9 - NO_BLACK_MARK
   *  505 - NO_PRINTER(
   *  507 - FAILED_TO_UPGRADE_FIRMWARE(
   * */
  getPrinterStatus: () => Promise<number>;
  CHIP_EVENT: 'CHIP_LOADED';
};

export default DvRnSunmiDevices as SunmiPrinter;

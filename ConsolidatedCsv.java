package test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsolidatedCsv {

    private static final String CSV_SEPARATOR = ",";

    public static void main(String[] args) throws IOException {
        System.out.println(LocalDateTime.now());
        ConsolidatedCsv consolidatedCsv = new ConsolidatedCsv();
        consolidatedCsv.test();
        System.out.println(LocalDateTime.now());
    }

    public void test() throws IOException {

        List<Promotion> proList = new ArrayList<>();
        List<Promotion> filteredProList = new ArrayList<>();

        File inputF = new File(
                "C:/mock data/m_promotion8.csv");
        InputStream inputFS = new FileInputStream(inputF);

        File inputF1 = new File(
                "C:/mock data/m_reward8.csv");
        int count =0;
        InputStream inputFS1 = new FileInputStream(inputF1);
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(inputFS, StandardCharsets.UTF_8))) {
            String data;
            while ((data = br.readLine()) != null) {
                count ++;
                data = data.replace("\"", "");
                String[] a = data.split(",");

                Promotion promotion = new Promotion();
                System.out.println(a[0] +count);
                promotion.setMasterStoreCode(String.format("%04d" ,Integer.parseInt(a[0])));
                promotion.setMaintenanceStoreMode(a[1]);
                promotion.setPromotionCode(String.format("%09d" ,Integer.parseInt(a[2])));
                promotion.setPromotionDescription(a[3]);
                promotion.setPromotionStartDate(a[4]);
                promotion.setPromotionStartTime(a[5]);
                promotion.setPromotionEndDate(a[6]);
                promotion.setPromotionEndTime(a[7]);
                promotion.setHeaderFlags(a[10]);
                promotion.setCreateRecordDate(a[23]);
                promotion.setLastUpdateDate(a[24]);
                promotion.setLastUpdateTime(a[25]);
                promotion.setTodaysUpdateFlag(a[26]);
                promotion.setTodaysDeleteFlag(a[27]);
                promotion.setSendFlag(a[28]);
                promotion.setSelectStoreMode(a[29]);
                proList.add(promotion);

            }
            filteredProList = proList.stream().filter(promotion -> promotion.getTodaysUpdateFlag().equals("1"))
                    .collect(Collectors.toList());
            System.out.println(("Filtered promotionList size : " + filteredProList.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Reward> rewList = new ArrayList<>();
        List<Reward> filteredRewList = new ArrayList<>();
        int count1 =0;
        try (final BufferedReader br2 = new BufferedReader(new InputStreamReader(inputFS1, StandardCharsets.UTF_8))) {
            String data1;

            while ((data1 = br2.readLine()) != null) {
                count1 ++;
                data1 = data1.replace("\"", "");
                String[] b = data1.split(",");
                Reward reward = new Reward();
                System.out.println(b[0] +count1);
                reward.setMasterStoreCode(String.format("%04d" ,Integer.parseInt(b[0])));
                reward.setMaintenanceStoreMode(b[1]);
                reward.setPromotionCode(String.format("%09d" ,Integer.parseInt(b[2])));
                reward.setCode(b[3]);
                reward.setSubCode(b[4]);
                reward.setOfferLevel(b[5]);
                reward.setMemberRewardLevelFlags(b[7]);
                reward.setRewardType(b[8]);
                reward.setProcess(b[9]);
                reward.setRewardValue(b[11]);
                reward.setRewardLevelType(b[17]);
                reward.setItemCode(b[18].substring(3));
                reward.setDepartment(b[19]);
                reward.setTender_ID(b[20]);
                reward.setTender_Sub_ID(b[21]);
                reward.setCouponItemCode(b[29]);
                rewList.add(reward);
            }
            filteredRewList = rewList.stream().filter(reward -> reward.getRewardType().equals("6"))
                    .collect(Collectors.toList());
            System.out.println("Filtered rewardList size : " + filteredRewList.size());

        } catch (IOException e) {
            e.printStackTrace();
        }

        ConsolidatedCsv consolidatedCsv = new ConsolidatedCsv();
        consolidatedCsv.consolidatedCsv(filteredProList, filteredRewList);
    }

    public void consolidatedCsv(List<Promotion> filteredProList, List<Reward> filteredRewList) {

        System.out.println("promotion filtered size : " + filteredProList.size());

        System.out.println("reward filtered size : " + filteredRewList.size());

        Map<String, Promotion> tmp = filteredProList.parallelStream()
                .collect(Collectors.toMap(
                        m -> m.getMasterStoreCode() + m.getMaintenanceStoreMode() + m.getPromotionCode(),
                        Function.identity()));
        System.out.println(tmp.values().size());
        for(String keys: tmp.keySet()) {
            System.out.println(keys);
        }
        List<ConsolidatedEntity> result = filteredRewList.parallelStream()
                .flatMap(two -> Optional
                        .ofNullable(tmp
                                .get(two.getMasterStoreCode() + two.getMaintenanceStoreMode() + two.getPromotionCode()))
                        .map(one -> {

                            if (one.getTodaysDeleteFlag().equals("1")) {
                                return Stream.of(new ConsolidatedEntity(
                                        two.getMasterStoreCode() , two.getMaintenanceStoreMode()
                                                , two.getPromotionCode() ,
                                        null,one.getPromotionDescription() , one.getPromotionStartDate(),
                                        one.getPromotionStartTime(),
                                        one.getPromotionEndDate() , one.getPromotionEndTime(),
                                        one.getTodaysUpdateFlag(), one.getTodaysDeleteFlag() , null ,null,null,null));
                            } else
                                return Stream.of(new ConsolidatedEntity(
                                        two.getMasterStoreCode() ,two.getMaintenanceStoreMode()
                                                , two.getPromotionCode() ,
                                        two.getCode(),one.getPromotionDescription(), one.getPromotionStartDate(),one.getPromotionStartTime(),
                                        one.getPromotionEndDate(),one.getPromotionEndTime(),one.getTodaysUpdateFlag(),one.getTodaysDeleteFlag(),
                                        two.getMemberRewardLevelFlags(), two.getRewardType(),
                                        two.getRewardValue(), two.getItemCode()));
                        }).orElse(null))
                .collect(Collectors.toList());

        System.out.println("result size : " + result.size());

        writeToCSV(result);

        // List<ConsolidatedEntity> consolidatedEntities = new ArrayList<>();
        //
        // for (Promotion p : filteredProList) {
        // String master = p.getMasterStoreCode();
        // String maintenanceStoreCode = p.getMaintenanceStoreCode();
        // String promotion = p.getPromotionCode();
        //
        // for (Reward r : filteredRewList) {
        //
        // if (master.equals(r.getMasterStoreCode()) &&
        // (maintenanceStoreCode.equals(r.getMaintenanceStoreCode()) &&
        // (promotion.equals(r.getPromotionCode())))) {
        //
        // ConsolidatedEntity consolidatedEntity = new ConsolidatedEntity();
        //
        // String pk = r.getMasterStoreCode() + r.getMaintenanceStoreCode() +
        // r.getPromotionCode() + r.getCode();
        // consolidatedEntity.setPk(pk);
        // consolidatedEntity.setSk("1");
        // consolidatedEntity.setJan(r.getItemCode());
        // consolidatedEntity.setRank(r.getMemberRewardLevelFlags());
        // consolidatedEntity.setPoint(r.getRewardValue());
        // consolidatedEntity.setPromotionDesc(p.getPromotionDescription());
        // consolidatedEntity.setType(r.getRewardType());
        // consolidatedEntity.setSdt(p.getPromotionStartDate() +
        // p.getPromotionStartTime());
        // consolidatedEntity.setEdt(p.getPromotionEndDate() + p.getPromotionEndTime());
        // consolidatedEntity.setTodaysUpdateFlag(p.getTodaysUpdateFlag());
        // consolidatedEntity.setTodaysDeleteFlag(p.getTodaysDeleteFlag());
        //
        // consolidatedEntities.add(consolidatedEntity);
        // }
        // }
        // }
        //
        // writeToCSV(consolidatedEntities);
        System.out.println("Created consolidated csv");
    }

    private static void writeToCSV(List<ConsolidatedEntity> consolidatedEntities) {
        try {
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("consolidated.csv"), "UTF-8"));

            for (ConsolidatedEntity consolidatedEntity : consolidatedEntities) {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(consolidatedEntity.getMasterStoreCode());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getMaintenanceStoreMode());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getPromotionCode());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getCode());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getPromotionDescription());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getPromotionStartDate());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getPromotionStartTime());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getPromotionEndDate());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getPromotionEndTime());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getTodaysUpdateFlag());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getTodaysDeleteFlag());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getMemberRewardLevelFlags());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getRewardType());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getRewardValue());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(consolidatedEntity.getItemCode());
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ConsolidatedEntity {

    private String MasterStoreCode;
    private String MaintenanceStoreMode;
    private String PromotionCode;
    private String Code;
    private String PromotionDescription;
    private String PromotionStartDate;
    private String PromotionStartTime;
    private String PromotionEndDate;
    private String PromotionEndTime;
    private String TodaysUpdateFlag;
    private String TodaysDeleteFlag;
    private String MemberRewardLevelFlags;
    private String RewardType;
    private String RewardValue;
    private String ItemCode;

    public ConsolidatedEntity(String masterStoreCode, String maintenanceStoreMode, String promotionCode, String code, String promotionDescription, String promotionStartDate, String promotionStartTime, String promotionEndDate, String promotionEndTime, String todaysUpdateFlag, String todaysDeleteFlag, String memberRewardLevelFlags, String rewardType, String rewardValue, String itemCode) {
        MasterStoreCode = masterStoreCode;
        MaintenanceStoreMode = maintenanceStoreMode;
        PromotionCode = promotionCode;
        Code = code;
        PromotionDescription = promotionDescription;
        PromotionStartDate = promotionStartDate;
        PromotionStartTime = promotionStartTime;
        PromotionEndDate = promotionEndDate;
        PromotionEndTime = promotionEndTime;
        TodaysUpdateFlag = todaysUpdateFlag;
        TodaysDeleteFlag = todaysDeleteFlag;
        MemberRewardLevelFlags = memberRewardLevelFlags;
        RewardType = rewardType;
        RewardValue = rewardValue;
        ItemCode = itemCode;
    }

    public String getMasterStoreCode() {
        return MasterStoreCode;
    }

    public void setMasterStoreCode(String masterStoreCode) {
        MasterStoreCode = masterStoreCode;
    }

    public String getMaintenanceStoreMode() {
        return MaintenanceStoreMode;
    }

    public void setMaintenanceStoreMode(String maintenanceStoreMode) {
        MaintenanceStoreMode = maintenanceStoreMode;
    }

    public String getPromotionCode() {
        return PromotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        PromotionCode = promotionCode;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public String getPromotionDescription() {
        return PromotionDescription;
    }

    public void setPromotionDescription(String promotionDescription) {
        PromotionDescription = promotionDescription;
    }

    public String getPromotionStartDate() {
        return PromotionStartDate;
    }

    public void setPromotionStartDate(String promotionStartDate) {
        PromotionStartDate = promotionStartDate;
    }

    public String getPromotionStartTime() {
        return PromotionStartTime;
    }

    public void setPromotionStartTime(String promotionStartTime) {
        PromotionStartTime = promotionStartTime;
    }

    public String getPromotionEndDate() {
        return PromotionEndDate;
    }

    public void setPromotionEndDate(String promotionEndDate) {
        PromotionEndDate = promotionEndDate;
    }

    public String getPromotionEndTime() {
        return PromotionEndTime;
    }

    public void setPromotionEndTime(String promotionEndTime) {
        PromotionEndTime = promotionEndTime;
    }

    public String getTodaysUpdateFlag() {
        return TodaysUpdateFlag;
    }

    public void setTodaysUpdateFlag(String todaysUpdateFlag) {
        TodaysUpdateFlag = todaysUpdateFlag;
    }

    public String getTodaysDeleteFlag() {
        return TodaysDeleteFlag;
    }

    public void setTodaysDeleteFlag(String todaysDeleteFlag) {
        TodaysDeleteFlag = todaysDeleteFlag;
    }

    public String getMemberRewardLevelFlags() {
        return MemberRewardLevelFlags;
    }

    public void setMemberRewardLevelFlags(String memberRewardLevelFlags) {
        MemberRewardLevelFlags = memberRewardLevelFlags;
    }

    public String getRewardType() {
        return RewardType;
    }

    public void setRewardType(String rewardType) {
        RewardType = rewardType;
    }

    public String getRewardValue() {
        return RewardValue;
    }

    public void setRewardValue(String rewardValue) {
        RewardValue = rewardValue;
    }

    public String getItemCode() {
        return ItemCode;
    }

    public void setItemCode(String itemCode) {
        ItemCode = itemCode;
    }

}
     class Promotion {

         private String MasterStoreCode;
         private String MaintenanceStoreMode;
         private String PromotionCode;
         private String PromotionDescription;
         private String PromotionStartDate;
         private String PromotionStartTime;
         private String PromotionEndDate;
         private String PromotionEndTime;
         private String PromotionLimit;
         private String PromotionLimitCustomOfferCode;
         private String HeaderFlags;
         private String AdvirtisementText1;
         private String AdvirtisementText2;
         private String AdvirtisementText3;
         private String AdvirtisementText4;
         private String EstimatedValue;
         private String ProcessingPriority;
         private String NumberOfPrizesAllowed;
         private String OddsOfWinning;
         private String DeteminationType;
         private String ExpiredDate;
         private String ReportDepartment;
         private String ReportCategory;
         private String CreateRecordDate;
         private String LastUpdateDate;
         private String LastUpdateTime;
         private String TodaysUpdateFlag;
         private String TodaysDeleteFlag;
         private String SendFlag;
         private String SelectStoreMode;

         public String getMaintenanceStoreMode() {
             return MaintenanceStoreMode;
         }

         public void setMaintenanceStoreMode(String maintenanceStoreMode) {
             MaintenanceStoreMode = maintenanceStoreMode;
         }

         public String getPromotionLimit() {
             return PromotionLimit;
         }

         public void setPromotionLimit(String promotionLimit) {
             PromotionLimit = promotionLimit;
         }

         public String getPromotionLimitCustomOfferCode() {
             return PromotionLimitCustomOfferCode;
         }

         public void setPromotionLimitCustomOfferCode(String promotionLimitCustomOfferCode) {
             PromotionLimitCustomOfferCode = promotionLimitCustomOfferCode;
         }

         public String getAdvirtisementText1() {
             return AdvirtisementText1;
         }

         public void setAdvirtisementText1(String advirtisementText1) {
             AdvirtisementText1 = advirtisementText1;
         }

         public String getAdvirtisementText2() {
             return AdvirtisementText2;
         }

         public void setAdvirtisementText2(String advirtisementText2) {
             AdvirtisementText2 = advirtisementText2;
         }

         public String getAdvirtisementText3() {
             return AdvirtisementText3;
         }

         public void setAdvirtisementText3(String advirtisementText3) {
             AdvirtisementText3 = advirtisementText3;
         }

         public String getAdvirtisementText4() {
             return AdvirtisementText4;
         }

         public void setAdvirtisementText4(String advirtisementText4) {
             AdvirtisementText4 = advirtisementText4;
         }

         public String getEstimatedValue() {
             return EstimatedValue;
         }

         public void setEstimatedValue(String estimatedValue) {
             EstimatedValue = estimatedValue;
         }

         public String getProcessingPriority() {
             return ProcessingPriority;
         }

         public void setProcessingPriority(String processingPriority) {
             ProcessingPriority = processingPriority;
         }

         public String getNumberOfPrizesAllowed() {
             return NumberOfPrizesAllowed;
         }

         public void setNumberOfPrizesAllowed(String numberOfPrizesAllowed) {
             NumberOfPrizesAllowed = numberOfPrizesAllowed;
         }

         public String getOddsOfWinning() {
             return OddsOfWinning;
         }

         public void setOddsOfWinning(String oddsOfWinning) {
             OddsOfWinning = oddsOfWinning;
         }

         public String getDeteminationType() {
             return DeteminationType;
         }

         public void setDeteminationType(String deteminationType) {
             DeteminationType = deteminationType;
         }

         public String getExpiredDate() {
             return ExpiredDate;
         }

         public void setExpiredDate(String expiredDate) {
             ExpiredDate = expiredDate;
         }

         public String getReportDepartment() {
             return ReportDepartment;
         }

         public void setReportDepartment(String reportDepartment) {
             ReportDepartment = reportDepartment;
         }

         public String getReportCategory() {
             return ReportCategory;
         }

         public void setReportCategory(String reportCategory) {
             ReportCategory = reportCategory;
         }

         public Promotion(String masterStoreCode, String maintenanceStoreMode, String promotionCode, String promotionDescription, String promotionStartDate, String promotionStartTime, String promotionEndDate, String promotionEndTime, String promotionLimit, String promotionLimitCustomOfferCode, String headerFlags, String advirtisementText1, String advirtisementText2, String advirtisementText3, String advirtisementText4, String estimatedValue, String processingPriority, String numberOfPrizesAllowed, String oddsOfWinning, String deteminationType, String expiredDate, String reportDepartment, String reportCategory, String createRecordDate, String lastUpdateDate, String lastUpdateTime, String todaysUpdateFlag, String todaysDeleteFlag, String sendFlag, String selectStoreMode) {
             MasterStoreCode = masterStoreCode;
             MaintenanceStoreMode = maintenanceStoreMode;
             PromotionCode = promotionCode;
             PromotionDescription = promotionDescription;
             PromotionStartDate = promotionStartDate;
             PromotionStartTime = promotionStartTime;
             PromotionEndDate = promotionEndDate;
             PromotionEndTime = promotionEndTime;
             PromotionLimit = promotionLimit;
             PromotionLimitCustomOfferCode = promotionLimitCustomOfferCode;
             HeaderFlags = headerFlags;
             AdvirtisementText1 = advirtisementText1;
             AdvirtisementText2 = advirtisementText2;
             AdvirtisementText3 = advirtisementText3;
             AdvirtisementText4 = advirtisementText4;
             EstimatedValue = estimatedValue;
             ProcessingPriority = processingPriority;
             NumberOfPrizesAllowed = numberOfPrizesAllowed;
             OddsOfWinning = oddsOfWinning;
             DeteminationType = deteminationType;
             ExpiredDate = expiredDate;
             ReportDepartment = reportDepartment;
             ReportCategory = reportCategory;
             CreateRecordDate = createRecordDate;
             LastUpdateDate = lastUpdateDate;
             LastUpdateTime = lastUpdateTime;
             TodaysUpdateFlag = todaysUpdateFlag;
             TodaysDeleteFlag = todaysDeleteFlag;
             SendFlag = sendFlag;
             SelectStoreMode = selectStoreMode;
         }


         public Promotion() {

         }

         public String getTodaysUpdateFlag() {
             return TodaysUpdateFlag;
         }

         public void setTodaysUpdateFlag(String todaysUpdateFlag) {
             TodaysUpdateFlag = todaysUpdateFlag;
         }

         public String getTodaysDeleteFlag() {
             return TodaysDeleteFlag;
         }

         public void setTodaysDeleteFlag(String todaysDeleteFlag) {
             TodaysDeleteFlag = todaysDeleteFlag;
         }

         public String getMasterStoreCode() {
             return MasterStoreCode;
         }

         public void setMasterStoreCode(String masterStoreCode) {
             MasterStoreCode = masterStoreCode;
         }


         public String getPromotionCode() {
             return PromotionCode;
         }

         public void setPromotionCode(String promotionCode) {
             PromotionCode = promotionCode;
         }

         public String getPromotionDescription() {
             return PromotionDescription;
         }

         public void setPromotionDescription(String promotionDescription) {
             PromotionDescription = promotionDescription;
         }

         public String getPromotionStartDate() {
             return PromotionStartDate;
         }

         public void setPromotionStartDate(String promotionStartDate) {
             PromotionStartDate = promotionStartDate;
         }

         public String getPromotionStartTime() {
             return PromotionStartTime;
         }

         public void setPromotionStartTime(String promotionStartTime) {
             PromotionStartTime = promotionStartTime;
         }

         public String getPromotionEndDate() {
             return PromotionEndDate;
         }

         public void setPromotionEndDate(String promotionEndDate) {
             PromotionEndDate = promotionEndDate;
         }

         public String getPromotionEndTime() {
             return PromotionEndTime;
         }

         public void setPromotionEndTime(String promotionEndTime) {
             PromotionEndTime = promotionEndTime;
         }

         public String getHeaderFlags() {
             return HeaderFlags;
         }

         public void setHeaderFlags(String headerFlags) {
             HeaderFlags = headerFlags;
         }

         public String getCreateRecordDate() {
             return CreateRecordDate;
         }

         public void setCreateRecordDate(String createRecordDate) {
             CreateRecordDate = createRecordDate;
         }

         public String getLastUpdateDate() {
             return LastUpdateDate;
         }

         public void setLastUpdateDate(String lastUpdateDate) {
             LastUpdateDate = lastUpdateDate;
         }

         public String getLastUpdateTime() {
             return LastUpdateTime;
         }

         public void setLastUpdateTime(String lastUpdateTime) {
             LastUpdateTime = lastUpdateTime;


         }

         public String getSendFlag() {
             return SendFlag;
         }

         public void setSendFlag(String sendFlag) {
             SendFlag = sendFlag;
         }

         public String getSelectStoreMode() {
             return SelectStoreMode;
         }

         public void setSelectStoreMode(String selectStoreMode) {
             SelectStoreMode = selectStoreMode;
         }
     }

        class Reward {

            private String MasterStoreCode;
            private String MaintenanceStoreMode;
            private String PromotionCode;
            private String Code;
            private String SubCode;
            private String OfferLevel;
            private String Flags;
            private String MemberRewardLevelFlags;
            private String RewardType;
            private String Process;
            private String CustomOfferCode;
            private String RewardValue;
            private String RewardLimit;
            private String RewardLimitCustomOfferCode;
            private String RewardTrigger;
            private String ApplyTo;
            private String ReportDepartment;
            private String RewardLevelType;
            private String ItemCode;
            private String Department;
            private String Tender_ID;
            private String Tender_Sub_ID;
            private String Tier;
            private String MiscType;
            private String TierEntries;
            private String PricEntries;
            private String PricRepeatAt;
            private String StringLength;
            private String String;
            private String CouponItemCode;

            public Reward(String masterStoreCode, String maintenanceStoreMode, String promotionCode, String code, String subCode, String offerLevel, String flags, String memberRewardLevelFlags, String rewardType, String process, String customOfferCode, String rewardValue, String rewardLimit, String rewardLimitCustomOfferCode, String rewardTrigger, String applyTo, String reportDepartment, String rewardLevelType, String itemCode, String department, String tender_ID, String tender_Sub_ID, String tier, String miscType, String tierEntries, String pricEntries, String pricRepeatAt, String stringLength, String string, String couponItemCode) {
                MasterStoreCode = masterStoreCode;
                MaintenanceStoreMode = maintenanceStoreMode;
                PromotionCode = promotionCode;
                Code = code;
                SubCode = subCode;
                OfferLevel = offerLevel;
                Flags = flags;
                MemberRewardLevelFlags = memberRewardLevelFlags;
                RewardType = rewardType;
                Process = process;
                CustomOfferCode = customOfferCode;
                RewardValue = rewardValue;
                RewardLimit = rewardLimit;
                RewardLimitCustomOfferCode = rewardLimitCustomOfferCode;
                RewardTrigger = rewardTrigger;
                ApplyTo = applyTo;
                ReportDepartment = reportDepartment;
                RewardLevelType = rewardLevelType;
                ItemCode = itemCode;
                Department = department;
                Tender_ID = tender_ID;
                Tender_Sub_ID = tender_Sub_ID;
                Tier = tier;
                MiscType = miscType;
                TierEntries = tierEntries;
                PricEntries = pricEntries;
                PricRepeatAt = pricRepeatAt;
                StringLength = stringLength;
                String = string;
                CouponItemCode = couponItemCode;
            }

            public Reward() {

            }

            public String getMasterStoreCode() {
                return MasterStoreCode;
            }

            public void setMasterStoreCode(String masterStoreCode) {
                MasterStoreCode = masterStoreCode;
            }

            public String getMaintenanceStoreMode() {
                return MaintenanceStoreMode;
            }

            public void setMaintenanceStoreMode(String maintenanceStoreMode) {
                MaintenanceStoreMode = maintenanceStoreMode;
            }

            public String getPromotionCode() {
                return PromotionCode;
            }

            public void setPromotionCode(String promotionCode) {
                PromotionCode = promotionCode;
            }

            public String getCode() {
                return Code;
            }

            public void setCode(String code) {
                Code = code;
            }

            public String getSubCode() {
                return SubCode;
            }

            public void setSubCode(String subCode) {
                SubCode = subCode;
            }

            public String getOfferLevel() {
                return OfferLevel;
            }

            public void setOfferLevel(String offerLevel) {
                OfferLevel = offerLevel;
            }

            public String getFlags() {
                return Flags;
            }

            public void setFlags(String flags) {
                Flags = flags;
            }

            public String getMemberRewardLevelFlags() {
                return MemberRewardLevelFlags;
            }

            public void setMemberRewardLevelFlags(String memberRewardLevelFlags) {
                MemberRewardLevelFlags = memberRewardLevelFlags;
            }

            public String getRewardType() {
                return RewardType;
            }

            public void setRewardType(String rewardType) {
                RewardType = rewardType;
            }

            public String getProcess() {
                return Process;
            }

            public void setProcess(String process) {
                Process = process;
            }

            public String getCustomOfferCode() {
                return CustomOfferCode;
            }

            public void setCustomOfferCode(String customOfferCode) {
                CustomOfferCode = customOfferCode;
            }

            public String getRewardValue() {
                return RewardValue;
            }

            public void setRewardValue(String rewardValue) {
                RewardValue = rewardValue;
            }

            public String getRewardLimit() {
                return RewardLimit;
            }

            public void setRewardLimit(String rewardLimit) {
                RewardLimit = rewardLimit;
            }

            public String getRewardLimitCustomOfferCode() {
                return RewardLimitCustomOfferCode;
            }

            public void setRewardLimitCustomOfferCode(String rewardLimitCustomOfferCode) {
                RewardLimitCustomOfferCode = rewardLimitCustomOfferCode;
            }

            public String getRewardTrigger() {
                return RewardTrigger;
            }

            public void setRewardTrigger(String rewardTrigger) {
                RewardTrigger = rewardTrigger;
            }

            public String getApplyTo() {
                return ApplyTo;
            }

            public void setApplyTo(String applyTo) {
                ApplyTo = applyTo;
            }

            public String getReportDepartment() {
                return ReportDepartment;
            }

            public void setReportDepartment(String reportDepartment) {
                ReportDepartment = reportDepartment;
            }

            public String getRewardLevelType() {
                return RewardLevelType;
            }

            public void setRewardLevelType(String rewardLevelType) {
                RewardLevelType = rewardLevelType;
            }

            public String getItemCode() {
                return ItemCode;
            }

            public void setItemCode(String itemCode) {
                ItemCode = itemCode;
            }

            public String getDepartment() {
                return Department;
            }

            public void setDepartment(String department) {
                Department = department;
            }

            public String getTender_ID() {
                return Tender_ID;
            }

            public void setTender_ID(String tender_ID) {
                Tender_ID = tender_ID;
            }

            public String getTender_Sub_ID() {
                return Tender_Sub_ID;
            }

            public void setTender_Sub_ID(String tender_Sub_ID) {
                Tender_Sub_ID = tender_Sub_ID;
            }

            public String getTier() {
                return Tier;
            }

            public void setTier(String tier) {
                Tier = tier;
            }

            public String getMiscType() {
                return MiscType;
            }

            public void setMiscType(String miscType) {
                MiscType = miscType;
            }

            public String getTierEntries() {
                return TierEntries;
            }

            public void setTierEntries(String tierEntries) {
                TierEntries = tierEntries;
            }

            public String getPricEntries() {
                return PricEntries;
            }

            public void setPricEntries(String pricEntries) {
                PricEntries = pricEntries;
            }

            public String getPricRepeatAt() {
                return PricRepeatAt;
            }

            public void setPricRepeatAt(String pricRepeatAt) {
                PricRepeatAt = pricRepeatAt;
            }

            public String getStringLength() {
                return StringLength;
            }

            public void setStringLength(String stringLength) {
                StringLength = stringLength;
            }

            public String getString() {
                return String;
            }

            public void setString(String string) {
                String = string;
            }

            public String getCouponItemCode() {
                return CouponItemCode;
            }

            public void setCouponItemCode(String couponItemCode) {
                CouponItemCode = couponItemCode;
            }
        }

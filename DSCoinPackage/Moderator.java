package DSCoinPackage;

import HelperClasses.Pair;

import java.util.List;

public class Moderator {

    public void initializeDSCoin(DSCoin_Honest DSObj, int coinCount) throws EmptyQueueException {
        Members moderator = new Members();
        moderator.UID = "Moderator";

        for (int i = 0; i < coinCount; i++) {
            int mem_idx = i % (DSObj.memberlist.length);
            String coinID = "" + (100000 + i);

            Transaction tr = new Transaction();
            tr.Destination = DSObj.memberlist[mem_idx];
            tr.Source = moderator;
            tr.coinsrc_block = null;
            tr.coinID = coinID;

            DSObj.pendingTransactions.AddTransactions(tr);

        }

        DSObj.latestCoinID = "" + (100000 + coinCount - 1);

        int count = 0;
        while(count < coinCount){
            Transaction[] trarray = new Transaction[DSObj.bChain.tr_count];

            for (int i = 0; i < DSObj.bChain.tr_count; i++) {
                trarray[i] = DSObj.pendingTransactions.RemoveTransaction();
            }

            TransactionBlock tB = new TransactionBlock(trarray);
            for (int j = 0; j < DSObj.bChain.tr_count; j++
            ) {
                Pair<String, TransactionBlock> newCoin = new Pair<String, TransactionBlock>(tB.trarray[j].coinID, tB);
                tB.trarray[j].Destination.mycoins.add(newCoin);
                selSortCoins(tB.trarray[j].Destination.mycoins);

            }

            DSObj.bChain.InsertBlock_Honest(tB);


            count += DSObj.bChain.tr_count;

        }




    }

    public void initializeDSCoin(DSCoin_Malicious DSObj, int coinCount) throws EmptyQueueException {
        Members moderator = new Members();
        moderator.UID = "Moderator";

        for (int i = 0; i < coinCount; i++) {
            int mem_idx = i % (DSObj.memberlist.length);
            String coinID = "" + (100000 + i);

            Transaction tr = new Transaction();
            tr.Destination = DSObj.memberlist[mem_idx];
            tr.Source = moderator;
            tr.coinsrc_block = null;
            tr.coinID = coinID;

            DSObj.pendingTransactions.AddTransactions(tr);

        }

        DSObj.latestCoinID = "" + (100000 + coinCount - 1);

        int count = 0;
        while(count < coinCount){
            Transaction[] trarray = new Transaction[DSObj.bChain.tr_count];

            for (int i = 0; i < DSObj.bChain.tr_count; i++) {
                trarray[i] = DSObj.pendingTransactions.RemoveTransaction();
            }

            TransactionBlock tB = new TransactionBlock(trarray);
            for (int j = 0; j < DSObj.bChain.tr_count; j++
            ) {
                Pair<String, TransactionBlock> newCoin = new Pair<String, TransactionBlock>(tB.trarray[j].coinID, tB);
                tB.trarray[j].Destination.mycoins.add(newCoin);
                selSortCoins(tB.trarray[j].Destination.mycoins);
            }

            DSObj.bChain.InsertBlock_Malicious(tB);


            count += DSObj.bChain.tr_count;

        }
    }

    public static void selSortCoins(List<Pair<String, TransactionBlock>> arr) {
        for (int i = 0; i < arr.size() - 1; i++) {
            int index = i;
            for (int j = i + 1; j < arr.size(); j++) {
                if (Integer.parseInt(arr.get(j).get_first()) < Integer.parseInt(arr.get(index).get_first())) {
                    index = j;//searching for lowest index
                }
            }
            Pair<String, TransactionBlock> smaller = arr.get(index);
            arr.set(index, arr.get(i));
            arr.set(i, smaller);
        }
    }
}

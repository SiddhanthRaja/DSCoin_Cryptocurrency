package DSCoinPackage;

import java.util.*;

import HelperClasses.MerkleTree;
import HelperClasses.Pair;
import HelperClasses.TreeNode;

public class Members {

    public String UID;

    //    todo: mycoins must be sorted by coinIDs while adding
    public List<Pair<String, TransactionBlock>> mycoins;
    public Transaction[] in_process_trans;

    public void initiateCoinsend(String destUID, DSCoin_Honest DSobj) {
        Members seller = null;

        for (Members mem : DSobj.memberlist
        ) {
            if (mem.UID.equals(destUID)) {
                seller = mem;
                break;

            }
        }

        Transaction tobj = new Transaction();

        tobj.Destination = seller;
        tobj.Source = this;
        tobj.coinID = mycoins.get(0).get_first();
        tobj.coinsrc_block = mycoins.get(0).get_second();

//        todo: iterate to find first null element in array to add trans
        for (int i = 0; i < in_process_trans.length; i++) {
            if (in_process_trans[i] == null) {
                in_process_trans[i] = tobj;
                break;

            }
        }


        DSobj.pendingTransactions.AddTransactions(tobj);
        mycoins.remove(0);

    }

    public void initiateCoinsend(String destUID, DSCoin_Malicious DSobj) {
        Members seller = null;

        for (Members mem : DSobj.memberlist
        ) {
            if (mem.UID.equals(destUID)) {
                seller = mem;
                break;

            }
        }

        Transaction tobj = new Transaction();

        tobj.Destination = seller;
        tobj.Source = this;
        tobj.coinID = mycoins.get(0).get_first();
        tobj.coinsrc_block = mycoins.get(0).get_second();

//        todo: iterate to find first null element in array to add trans
        for (int i = 0; i < in_process_trans.length; i++) {
            if (in_process_trans[i] == null) {
                in_process_trans[i] = tobj;
                break;

            }
        }


        DSobj.pendingTransactions.AddTransactions(tobj);
        mycoins.remove(0);
    }

    public Pair<List<Pair<String, String>>, List<Pair<String, String>>> finalizeCoinsend(Transaction tobj, DSCoin_Honest DSObj) throws MissingTransactionException {
        TransactionBlock tB = null;
        TransactionBlock block = DSObj.bChain.lastBlock;

        while (block != null) {
            for (Transaction tr : block.trarray
            ) {
                if (tr == tobj) {
                    tB = block;
                    break;
                }
            }

            if (block == tB) {
                break;
            } else {
                block = block.previous;
            }
        }

        if (tB == null) {
            throw new MissingTransactionException();
        }


//            todo: iterate to find tobj in in_process_trans to delete it. Be careful about the order of nulls!!
        for (int i = 0; i < in_process_trans.length; i++) {
            if (in_process_trans[i] == tobj) {
                in_process_trans[i] = null;
                break;

            }
        }

        Pair<String, TransactionBlock> sentCoin = new Pair<>(tobj.coinID, tB);
        tobj.Destination.mycoins.add(sentCoin);
        selSortCoins(tobj.Destination.mycoins);

//        start finding the lists

        ArrayList<Pair<String, String>> verifDoc = new ArrayList<Pair<String, String>>();
        TransactionBlock initblock = DSObj.bChain.lastBlock;

        String prev_dgst = "DSCoin";
        while (initblock != null && initblock != tB.previous) {

            if (initblock.previous != null) {
                prev_dgst = initblock.previous.dgst;
            }

            Pair<String, String> dgstPair = new Pair<String, String>(initblock.dgst, prev_dgst + '#' + initblock.trsummary + '#' + initblock.nonce);
            verifDoc.add(dgstPair);

            initblock = initblock.previous;
        }

        if (tB.previous != null) {
            prev_dgst = tB.previous.dgst;
        }

        Pair<String, String> lastPair = new Pair<String, String>(prev_dgst, null);
        verifDoc.add(lastPair);
        Collections.reverse(verifDoc);
//        ReverseList(verifDoc);

//        todo: find tNode containing tobj in MTree

        TreeNode tNode = null;
        for (TreeNode leaf : tB.Tree.leafNodes
        ) {

            if (leaf.val.equals(tB.Tree.get_str(tobj))) {
                tNode = leaf;
                break;
            }
        }

        if(tNode == null){
            System.out.println("tNode containing tobj not found in MTree");
        }

        ArrayList<Pair<String, String>> path = new ArrayList<Pair<String, String>>();

        TreeNode initnode = tNode.parent;

        while (initnode != null) {
            Pair<String, String> siblings = new Pair<String, String>(initnode.left.val, initnode.right.val);
            path.add(siblings);

            initnode = initnode.parent;
        }

        Pair<String, String> rootonly = new Pair<String, String>(tB.Tree.rootnode.val, null);
        path.add(rootonly);

        return new Pair<>(path, verifDoc);

    }

    public void MineCoin(DSCoin_Honest DSObj) throws EmptyQueueException {
        Transaction[] trarray = new Transaction[DSObj.bChain.tr_count];

        //todo: add exactly tr_count -1 trans to trarray irrespective of repetitions
        int count = 0;
        while (count < DSObj.bChain.tr_count - 1) {
            Transaction newtr = DSObj.pendingTransactions.RemoveTransaction();

//        todo: check validity of all transactions. See FAQs

//            boolean isValid = DSObj.bChain.lastBlock.checkTransaction(newtr);
            boolean isValid = true;
            for (Transaction tr : trarray
            ) {
                if (tr == null) {
                    break;
                }
                else if (tr.coinID.equals(newtr.coinID)) {
                    isValid = false;
                    break;
                }
            }

            if (isValid) {
                trarray[count] = newtr;
                count++;
            }
        }

        Transaction minerRewardTransaction = new Transaction();
        minerRewardTransaction.coinID = "" + (Integer.parseInt(DSObj.latestCoinID) + 1);
        minerRewardTransaction.Source = null;
        minerRewardTransaction.coinsrc_block = null;
        minerRewardTransaction.Destination = this;

        trarray[DSObj.bChain.tr_count - 1] = minerRewardTransaction;

        TransactionBlock tB = new TransactionBlock(trarray);

        Pair<String, TransactionBlock> newCoin = new Pair<>(
                minerRewardTransaction.coinID, tB
        );

        mycoins.add(newCoin);
        selSortCoins(mycoins);
        DSObj.latestCoinID = minerRewardTransaction.coinID;


        DSObj.bChain.InsertBlock_Honest(tB);



    }

    public void MineCoin(DSCoin_Malicious DSObj) throws EmptyQueueException {

        Transaction[] trarray = new Transaction[DSObj.bChain.tr_count];

        //todo: add exactly tr_count -1 trans to trarray irrespective of repetitions
        int count = 0;
        while (count < DSObj.bChain.tr_count - 1) {
            Transaction newtr = DSObj.pendingTransactions.RemoveTransaction();

//        todo: check validity of all transactions. See FAQs

//            boolean isValid = DSObj.bChain.lastBlock.checkTransaction(newtr);
            boolean isValid = true;
            for (Transaction tr : trarray
            ) {
                if (tr == null) {
                    break;
                }
                else if (tr.coinID.equals(newtr.coinID)) {
                    isValid = false;
                    break;
                }
            }

            if (isValid) {
                trarray[count] = newtr;
                count++;
            }
        }

        Transaction minerRewardTransaction = new Transaction();
        minerRewardTransaction.coinID = "" + (Integer.parseInt(DSObj.latestCoinID) + 1);
        minerRewardTransaction.Source = null;
        minerRewardTransaction.coinsrc_block = null;
        minerRewardTransaction.Destination = this;

        trarray[DSObj.bChain.tr_count - 1] = minerRewardTransaction;

        TransactionBlock tB = new TransactionBlock(trarray);

        Pair<String, TransactionBlock> newCoin = new Pair<>(
                minerRewardTransaction.coinID, tB
        );

        mycoins.add(newCoin);
        selSortCoins(mycoins);
        DSObj.latestCoinID = minerRewardTransaction.coinID;


        DSObj.bChain.InsertBlock_Malicious(tB);


    }

    public List<Pair<String, String>> PathToRoot(TreeNode node) {

        ArrayList<Pair<String, String>> path = new ArrayList<Pair<String, String>>();

        TreeNode initnode = node;

        while (initnode.parent != null) {

            Pair<String, String> siblings;
            if (initnode.parent.right != initnode) {
                siblings = new Pair<String, String>(initnode.val, initnode.parent.right.val);
            } else {
                siblings = new Pair<String, String>(initnode.parent.left.val, initnode.val);
            }
            path.add(siblings);

            initnode = initnode.parent;

        }

        Pair<String, String> rootonly = new Pair<String, String>(initnode.val, null);
        path.add(rootonly);

        return path;
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

    public static void ReverseList(List<Pair<String, String>> list) {
        List<Pair<String, String>> reverseList = new ArrayList<Pair<String, String>>();

        for (int i = list.size() - 1; i >= 0; i--) {
            reverseList.add(list.get(i));
        }

        list = reverseList;
    }

}

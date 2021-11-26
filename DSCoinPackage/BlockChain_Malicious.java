package DSCoinPackage;

import HelperClasses.CRF;
import HelperClasses.MerkleTree;

import java.util.Objects;

public class BlockChain_Malicious {

    public int tr_count;
    public static final String start_string = "DSCoin";
    public TransactionBlock[] lastBlocksList;

    public static boolean checkTransactionBlock(TransactionBlock tB) {
        CRF obj = new CRF(64);
        String prev_dgst = start_string;

        if (tB.previous != null) {
            prev_dgst = tB.previous.dgst;
        }

        boolean isValid = ( obj.Fn(prev_dgst + '#' + tB.trsummary + '#' + tB.nonce).equals(tB.dgst)) && tB.dgst.substring(0, 4).equals("0000");

        if (!isValid) {
            return false;
        }

        MerkleTree mTree = new MerkleTree();
        isValid = tB.trsummary.equals(mTree.Build(tB.trarray));

        if (!isValid) {
            return false;
        }

//        todo: checkTransaction throws nullPointer => uses coinsrc_block which is null for Moderator generated coins
        for (int i = 0; i < tB.trarray.length; i++) {
            if (!tB.checkTransaction(tB.trarray[i])) {
                return false;
            }
        }
        return true;
    }

    public TransactionBlock FindLongestValidChain() {

        int len_max = 0;
        TransactionBlock lastBlock = null;
        for (TransactionBlock tb : lastBlocksList
        ) {
            int len = 0;
            TransactionBlock block = tb;
            while (block != null) {
                if (!checkTransactionBlock(block)) {
                    len = 0;
                    tb = block.previous;

                } else {
                    len++;
                }
                block = block.previous;
            }

            if (len > len_max) {
                len_max = len;
                lastBlock = tb;
            }
        }
        return lastBlock;
    }

    public void InsertBlock_Malicious(TransactionBlock newBlock) {
        TransactionBlock lastBlock = FindLongestValidChain();

        CRF obj = new CRF(64);
        String prev_dgst = start_string;

        if (lastBlock != null) {
            prev_dgst = lastBlock.dgst;
        }

        int initNonce = 1000000001;
        String dgst = obj.Fn(prev_dgst + '#' + newBlock.trsummary + '#' + initNonce);

        while (!dgst.substring(0, 4).equals("0000") ) {
            initNonce++;
            dgst = obj.Fn(prev_dgst + '#' + newBlock.trsummary + '#' + initNonce);

        }

        newBlock.nonce = "" + initNonce;
        newBlock.dgst = dgst;
        newBlock.previous = lastBlock;

//      todo: length of lastBlocks List may/ mayn't increase
        boolean isInLastBlocksList = false;
        for (int j = 0; j < lastBlocksList.length; j++) {
            if (lastBlocksList[j] == lastBlock) {
                lastBlocksList[j] = newBlock;
                isInLastBlocksList = true;
                break;
            }
        }

        if (!isInLastBlocksList) {
            // todo: iterate to find last-null element in array to add trans
            for (int i = 0; i < lastBlocksList.length; i++) {
                if (lastBlocksList[i] == null) {
                    lastBlocksList[i] = newBlock;
                    break;
                }
            }
        }


    }
}

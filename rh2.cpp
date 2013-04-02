#include <iostream>
#include <fstream>
#include <vector>
#include <string>
#include <cassert>

struct Num {
    long long num;
    long long den;
    bool isnull;

    static Num ZERO() {
            return Num(0L, 1L);
    }
    static Num ONE() {
            return Num(1L, 1L);
    }
    static Num NEG_ONE() {
            return Num(-1L, 1L);
    }

    Num() {
        this->isnull = true;
    }

    Num(long num, long den) {
        this->num = num;
        this->den = den;
        this->isnull = false;
    }

    int compare(Num arg) {
            //a/b ? c/d      |*bd
            //ad ? bc
            long long l = num * arg.den;
            long long r = den * arg.num;
            if (l == r)
                    return 0;
            if (l > r)
                    return 1;
            return -1;
    }

    Num add(Num arg) {
            Num res(num * arg.den, den * arg.den);
            res.num = res.num + arg.num * den;

            res.shorten();
            return res;
    }

    void shorten() {
            while (num != 0 && num % 2 == 0L && den % 2 == 0L) {
                    if (den == 0L)
                        throw "";
                    num /= 2;
                    den /= 2;
            }
    }
};

std::vector<int> graph[30];
int edgCount;
bool colors[30];
std::vector<int> edgIndex[30][30];
Num maskResults[1200000];
int normMasks[1200000];
int usageMasks[1200000];

struct dfs {
    dfs(int mask) {
        init(mask);
    }

    void init(int mask) {
        edgUsedMask = 0;
        this->mask = mask;
    }

    int edgUsedMask;
    int mask;
    int normalizeDfs(int cVertex) {
        for (int i = 0; i < graph[cVertex].size(); ++i) {
            int dest = graph[cVertex][i];
            for (int j = 0; j < edgIndex[cVertex][dest].size(); ++j) {
                int currIndex = edgIndex[cVertex][dest][j];
                int cbit = (1 << currIndex);

                if (!(cbit & mask) || cbit & edgUsedMask) //no edge or used
                        continue;

                edgUsedMask |= cbit;
                normalizeDfs(dest);
            }
        }
        return edgUsedMask;
    }

    int exec() {
        if (usageMasks[mask] >= 0)
            return usageMasks[mask];
        return normalizeDfs(1);
    }
};

Num eval(int mask) {
    if (mask == 0) {
        maskResults[0] = Num::ZERO();
        return maskResults[0];
    }

    Num minR;
    Num maxB;

    for (int i = 0; i < edgCount; ++i) {
            int cbit = (1 << i);
            if (cbit & mask) {
                    int nmask = normMasks[cbit ^ mask];

                    Num cres;
                    if (!maskResults[nmask].isnull) {
                            cres = maskResults[nmask];
                    } else {
                        //throw "null";
                            cres = eval(nmask);
                            maskResults[nmask] = cres;
                    }

                    if (colors[i]) {
                            if (minR.isnull || cres.compare(minR) < 0)
                                    minR = cres;
                    } else {
                            if (maxB.isnull || cres.compare(maxB) > 0)
                                    maxB = cres;
                    }
            }
    }

    if (minR.isnull) {
            return maxB.add(Num::ONE());
    }

    if (maxB.isnull) {
            return minR.add(Num::NEG_ONE());
    }

    if (minR.compare(Num::ZERO()) > 0 && maxB.compare(Num::ZERO()) < 0) {
            return Num::ZERO();
    }

    Num lborder = Num::ZERO(), rborder = Num::ZERO();
    if (minR.compare(Num::ZERO()) >= 0 && maxB.compare(Num::ZERO()) >= 0) {
            while (minR.compare(rborder) >= 0) {
                    rborder = rborder.add(Num::ONE());
            }
    } else {
            while (maxB.compare(lborder) <= 0) {
                    lborder = lborder.add(Num::NEG_ONE());
            }
    }

    while (true) {
        Num nborder = lborder.add(rborder);
        nborder.den = nborder.den * 2; //(l+r)/2

        if (nborder.compare(maxB) > 0 && nborder.compare(minR) < 0)
                return nborder;

        if (nborder.compare(maxB) <= 0 && nborder.compare(minR) <= 0) {
                lborder = nborder;
        }

        if (nborder.compare(maxB) >= 0 && nborder.compare(minR) >= 0) {
                rborder = nborder;
        }
    }
}


int main(int argc, char **argv) {
    std::ifstream fis("bluered.in");
    std::ofstream fos("bluered.out");

    int vertCount = 0;
    fis >> vertCount;
    fis >> edgCount;

    //for (int i = 0; i <= vertCount; ++i) {
    //        graph[i] = std::vector<int>();
    //}

    for (int i = 0; i < edgCount; ++i) {
            int from = 0;
            fis >> from;
            int to = 0;
            fis >> to;
            int col = 0;
            fis >> col;
            bool color = (col == 1); //true == red
            graph[from].push_back(to);
            if (from != to)
                graph[to].push_back(from);
            edgIndex[from][to].push_back(i);
            if (from != to)
                edgIndex[to][from].push_back(i);
            colors[i] = color;
    }

    for (int i = 0; i < (1 << edgCount); ++i) {
        normMasks[i] = -1;
        usageMasks[i] = -1;
    }

    dfs d(0);
    for (int i = 0; i < (1 << edgCount); ++i) {
        d.init(i);
        usageMasks[i] = d.exec();
        normMasks[i] = i & usageMasks[i];

        int cmask = normMasks[i];
        if (maskResults[cmask].isnull) {
            maskResults[cmask] = eval(cmask);
        }

    }

    int initMask = (1 << edgCount) - 1;
    //assert (normMasks[initMask] == initMask);

    Num res = maskResults[initMask];
    //Num res = eval(initMask);
    res.shorten();
    fos << res.num << ' ' << res.den << "\n";

    return 0;
}


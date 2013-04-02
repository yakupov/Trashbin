#include <iostream>
#include <fstream>
#include <vector>
#include <string>

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

            //res.shorten();
            return res;
    }

    void shorten() {
            while (num != 0 && num % 2 == 0L && den % 2 == 0L) {
                    if (den == 0L)
                        throw NULL;
                    num /= 2;
                    den /= 2;
            }
    }
};

std::vector<int> graph[30];
int edgCount;
bool colors[30];
int edgIndex[30][30];
Num maskResults[1200000];
int normUseMasks[1200000];


int normalizeDfs(int mask, int cVertex, int edgUsedMask) {
    if (normUseMasks[mask] >= 0)
        return normUseMasks[mask];


    for (int i = 0; i < graph[cVertex].size(); ++i) {
        int dest = graph[cVertex][i];
            int currIndex = edgIndex[cVertex][dest];
            int cbit = (1 << currIndex);

            if (!(cbit & mask)) //no edge
                    continue;

            int nUMask = cbit | edgUsedMask;
            if (nUMask == edgUsedMask) //used
                    continue;

            edgUsedMask = normalizeDfs(mask, dest, nUMask);
    }
    return edgUsedMask;
}

Num eval(int mask) {
    if (mask == 0)
            return Num::ZERO();

    Num minR;
    Num maxB;

    for (int i = 0; i < edgCount; ++i) {
            int cbit = (1 << i);
            if (cbit & mask) {
                    int nmask = cbit ^ mask;
                    int useMask =  normUseMasks[nmask];//normalizeDfs(nmask, 1, 0);

                    nmask &= useMask;

                    Num cres;
                    if (!maskResults[nmask].isnull) {
                            cres = maskResults[nmask];
                    } else {
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

    for (int i = 0; i <= vertCount; ++i) {
            graph[i] = std::vector<int>();
    }

    for (int i = 0; i < edgCount; ++i) {
            int from = 0;
            fis >> from;
            int to = 0;
            fis >> to;
            int col = 0;
            fis >> col;
            bool color = (col == 1); //true == red
            graph[from].push_back(to);
            graph[to].push_back(from);
            edgIndex[from][to] = i;
            edgIndex[to][from] = i;
            colors[i] = color;
    }

    for (int i = 0; i < (1 << edgCount); ++i) {
        normUseMasks[i] = -1;
    }

    for (int i = 0; i < (1 << edgCount); ++i) {
        normUseMasks[i] = normalizeDfs(i, 1, 0);
    }

    int initMask = (1 << edgCount) - 1;
    for (int i = 0; i < edgCount; ++i) {
            initMask |= (1 << i);
    }

    Num res = eval(initMask);
    res.shorten();
    fos << res.num << ' ' << res.den << "\n";

    return 0;
}


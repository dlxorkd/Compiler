{
print("Input Int, Float, String,  Boolean or Null : ");
a = readLine();
if(a[0] != '"') {
if(a == "true" ||a == "false" ||a == "null"){}
else {
flag = 0;
 for(ijk=0; ijk<a.length; ijk++) {
if(a[ijk] == '.') {
flag = 1;
}
}
if(flag == 1) {
a = parseFloat(a);
}
if(flag == 0) {
a = parseInt(a);
}
}
}else {
a = a.substring(1, a.length - 1);
}
print("a = ",a);
print("==========================================");
print("Input 1 dimension array size : ");
size = readLine();
if(size[0] != '"') {
if(size == "true" ||size == "false" ||size == "null"){}
else {
flag = 0;
 for(ijk=0; ijk<size.length; ijk++) {
if(size[ijk] == '.') {
flag = 1;
}
}
if(flag == 1) {
size = parseFloat(size);
}
if(flag == 0) {
size = parseInt(size);
}
}
}else {
size = size.substring(1, size.length - 1);
}
print("Input 1 dimension array : ");
a = [0];
for(i = 0;i<size;i++)
{
a[i] = readLine();
if(a[i][0] != '"') {
if(a[i] == "true" ||a[i] == "false" ||a[i] == "null"){}
else {
flag = 0;
 for(ijk=0; ijk<a[i].length; ijk++) {
if(a[i][ijk] == '.') {
flag = 1;
}
}
if(flag == 1) {
a[i] = parseFloat(a[i]);
}
if(flag == 0) {
a[i] = parseInt(a[i]);
}
}
}else {
a[i] = a[i].substring(1, a[i].length - 1);
}
}
for(i = 0;i<size;i++)
{
print("a[",i,"] = ",a[i]);
}
print("==========================================");
print("Input 2 dimension array second size : ");
ssize = readLine();
if(ssize[0] != '"') {
if(ssize == "true" ||ssize == "false" ||ssize == "null"){}
else {
flag = 0;
 for(ijk=0; ijk<ssize.length; ijk++) {
if(ssize[ijk] == '.') {
flag = 1;
}
}
if(flag == 1) {
ssize = parseFloat(ssize);
}
if(flag == 0) {
ssize = parseInt(ssize);
}
}
}else {
ssize = ssize.substring(1, ssize.length - 1);
}
print("Input 2 dimension array : ");
a = [[0]];
for(i = 0;i<1;i++)
{
for(j = 0;j<ssize;j++)
{
a[i][j] = readLine();
if(a[i][j][0] != '"') {
if(a[i][j] == "true" ||a[i][j] == "false" ||a[i][j] == "null"){}
else {
flag = 0;
 for(ijk=0; ijk<a[i][j].length; ijk++) {
if(a[i][j][ijk] == '.') {
flag = 1;
}
}
if(flag == 1) {
a[i][j] = parseFloat(a[i][j]);
}
if(flag == 0) {
a[i][j] = parseInt(a[i][j]);
}
}
}else {
a[i][j] = a[i][j].substring(1, a[i][j].length - 1);
}
}
}
for(i = 0;i<1;i++)
{
for(j = 0;j<ssize;j++)
{
print("a[",i,"][",j,"] = ",a[i][j]);
}
}
}